package id.my.selo.sekost.feature.issues

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import id.my.selo.sekost.core.data.repository.IssueRepository
import id.my.selo.sekost.core.data.repository.PropertyRepository
import id.my.selo.sekost.core.data.repository.RoomRepository
import id.my.selo.sekost.core.domain.TimeProvider
import id.my.selo.sekost.core.model.Issue
import id.my.selo.sekost.core.model.IssueStatus
import id.my.selo.sekost.core.model.IssueType
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** A room choice for the "report issue" picker, labelled with its property. */
data class RoomOption(val roomId: Long, val label: String, val propertyName: String)

data class IssueItem(val issue: Issue, val room: RoomOption?)

data class IssuesUiState(
    val status: IssueStatus = IssueStatus.OPEN,
    val type: IssueType? = null,
    val items: List<IssueItem> = emptyList(),
    val openCount: Int = 0,
    val resolvedCount: Int = 0,
    val rooms: List<RoomOption> = emptyList(),
)

@HiltViewModel
class IssuesViewModel @Inject constructor(
    private val issues: IssueRepository,
    rooms: RoomRepository,
    properties: PropertyRepository,
    private val time: TimeProvider,
) : ViewModel() {

    private val status = MutableStateFlow(IssueStatus.OPEN)
    private val type = MutableStateFlow<IssueType?>(null)

    private val roomOptions = combine(rooms.observeAll(), properties.observeAll()) { roomList, props ->
        val names = props.associate { it.id to it.name }
        roomList.map { RoomOption(it.id, it.label, names[it.propertyId].orEmpty()) }
            .sortedWith(compareBy({ it.propertyName }, { it.label.length }, { it.label }))
    }

    val uiState: StateFlow<IssuesUiState> = combine(issues.observeAll(), roomOptions, status, type) { all, options, s, t ->
        val byId = options.associateBy { it.roomId }
        IssuesUiState(
            status = s,
            type = t,
            items = all.filter { it.status == s && (t == null || it.type == t) }.map { IssueItem(it, byId[it.roomId]) },
            openCount = all.count { it.status == IssueStatus.OPEN },
            resolvedCount = all.count { it.status == IssueStatus.RESOLVED },
            rooms = options,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), IssuesUiState())

    fun setStatus(value: IssueStatus) {
        status.value = value
    }

    fun setType(value: IssueType?) {
        type.value = value
    }

    fun report(roomId: Long, type: IssueType, title: String, description: String) {
        viewModelScope.launch {
            issues.save(Issue(roomId = roomId, type = type, title = title, description = description, reportedOn = time.today()))
        }
    }

    fun resolve(id: Long) {
        viewModelScope.launch { issues.resolve(id, time.today()) }
    }

    fun reopen(id: Long) {
        viewModelScope.launch { issues.reopen(id) }
    }
}
