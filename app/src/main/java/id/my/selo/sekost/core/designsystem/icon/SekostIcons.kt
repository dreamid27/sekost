package id.my.selo.sekost.core.designsystem.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Sekost's bespoke line-icon set.
 *
 * Rules every icon follows so the set stays coherent:
 *  - 24×24 grid, 2dp safe padding, 1.75 stroke, round caps & joins.
 *  - Corners use a 1dp radius; architectural shapes (roofs, doors) echo the brand mark.
 *  - Stroke color is black and gets tinted by `Icon`, so never bake colors in.
 */
object SekostIcons {
    val Home by lazy {
        lineIcon("Home", "M3.5 10.5L12 3.75l8.5 6.75", "M5.5 9v10.25c0 .55.45 1 1 1H10v-5.5h4v5.5h3.5c.55 0 1-.45 1-1V9")
    }
    val Kost by lazy {
        lineIcon(
            "Kost",
            "M3.75 20.25h16.5",
            "M6 20.25V5.25c0-.69.56-1.25 1.25-1.25h9.5c.69 0 1.25.56 1.25 1.25v15",
            "M9.5 8h1M13.5 8h1M9.5 11.75h1M13.5 11.75h1",
            "M10.5 20.25v-4.5h3v4.5",
        )
    }
    val House by lazy {
        lineIcon(
            "House",
            "M3 11L12 4l9 7",
            "M5.25 9.25v11h13.5v-11",
            "M15.75 6.6V4.25h2.5v4.3",
            "M10 20.25V15.5c0-.28.22-.5.5-.5h3c.28 0 .5.22.5.5v4.75",
        )
    }
    val Door by lazy {
        lineIcon(
            "Door",
            "M4 20.5h16",
            "M6.25 20.5V4.75c0-.41.34-.75.75-.75h10c.41 0 .75.34.75.75V20.5",
            "M14.75 12.25v.5",
        )
    }
    val Bill by lazy {
        lineIcon(
            "Bill",
            "M6 3.75h12v16.5l-2.25-1.5-1.75 1.5-2-1.5-2 1.5-1.75-1.5L6 20.25z",
            "M9 8h6M9 11.5h6M9 15h3.5",
        )
    }
    val Toolbox by lazy {
        lineIcon(
            "Toolbox",
            "M3.75 9.5h16.5v9.25c0 .55-.45 1-1 1H4.75c-.55 0-1-.45-1-1z",
            "M8.5 9.5V6.75c0-.55.45-1 1-1h5c.55 0 1 .45 1 1V9.5",
            "M3.75 13.5h16.5",
            "M10.5 13.5v1.75h3V13.5",
        )
    }
    val Complaint by lazy {
        lineIcon(
            "Complaint",
            "M4 5.75c0-.55.45-1 1-1h14c.55 0 1 .45 1 1v10c0 .55-.45 1-1 1h-8.5L6.5 20v-3.25H5c-.55 0-1-.45-1-1z",
            "M12 8v3.75",
            "M12 14.25v.25",
        )
    }
    val Clipboard by lazy {
        lineIcon(
            "Clipboard",
            "M8 4.75H6.75c-.55 0-1 .45-1 1v13.5c0 .55.45 1 1 1h10.5c.55 0 1-.45 1-1V5.75c0-.55-.45-1-1-1H16",
            "M9 3.5h6v2.75H9z",
            "M9 11h6M9 14.75h4",
        )
    }
    val Sliders by lazy {
        lineIcon(
            "Sliders",
            "M4 7h8.5M17.5 7H20M4 17h2.5M11.5 17H20",
            "M15 4.75a2.25 2.25 0 1 0 0 4.5a2.25 2.25 0 1 0 0-4.5z",
            "M9 14.75a2.25 2.25 0 1 0 0 4.5a2.25 2.25 0 1 0 0-4.5z",
        )
    }
    val Person by lazy {
        lineIcon(
            "Person",
            "M12 4.25a3.75 3.75 0 1 0 0 7.5a3.75 3.75 0 1 0 0-7.5z",
            "M5 20c.6-3.3 3.4-5.5 7-5.5s6.4 2.2 7 5.5",
        )
    }
    val Key by lazy {
        lineIcon(
            "Key",
            "M4.25 12a3.5 3.5 0 1 0 7 0a3.5 3.5 0 1 0-7 0z",
            "M11.25 12h8.5",
            "M16.75 12v3M19.75 12v2.25",
        )
    }
    val Wallet by lazy {
        lineIcon(
            "Wallet",
            "M3.75 7.25c0-1.1.9-2 2-2h11.5v3.5",
            "M3.75 7.25v10.5c0 1.1.9 2 2 2h13.5c.55 0 1-.45 1-1V9.75c0-.55-.45-1-1-1H5.75c-1.1 0-2-.9-2-2z",
            "M16.25 14.25h.5",
        )
    }
    val Tag by lazy {
        lineIcon(
            "Tag",
            "M3.75 12.25V4.75c0-.55.45-1 1-1h7.5l8 8-8.5 8.5z",
            "M8.25 8.25h.5",
        )
    }
    val Calendar by lazy {
        lineIcon(
            "Calendar",
            "M4.5 6.5c0-.55.45-1 1-1h13c.55 0 1 .45 1 1v12.5c0 .55-.45 1-1 1h-13c-.55 0-1-.45-1-1z",
            "M4.5 10h15",
            "M8.5 3.5V7M15.5 3.5V7",
        )
    }
    val Phone by lazy {
        lineIcon(
            "Phone",
            "M5.25 4.5h2.9l1.4 3.75-1.9 1.2a9.5 9.5 0 0 0 4.9 4.9l1.2-1.9 3.75 1.4v2.9" +
                "c0 .83-.67 1.5-1.5 1.5A12.25 12.25 0 0 1 3.75 6c0-.83.67-1.5 1.5-1.5z",
        )
    }
    val Chat by lazy {
        lineIcon(
            "Chat",
            "M12 3.75a8.25 8.25 0 0 0-7.1 12.45L3.75 20.25l4.15-1.1A8.25 8.25 0 1 0 12 3.75z",
            "M8.75 12h.5M11.75 12h.5M14.75 12h.5",
        )
    }
    val CheckOut by lazy {
        lineIcon(
            "CheckOut",
            "M10 4.5H6c-.55 0-1 .45-1 1v13c0 .55.45 1 1 1h4",
            "M15 8l4 4-4 4",
            "M19 12H9.5",
        )
    }
    val Spark by lazy {
        lineIcon(
            "Spark",
            "M12 3.5l1.7 5.3 5.3 1.7-5.3 1.7L12 17.5l-1.7-5.3L5 10.5l5.3-1.7z",
            "M18.5 16.5l.6 1.9 1.9.6-1.9.6-.6 1.9-.6-1.9-1.9-.6 1.9-.6z",
        )
    }
    val Moon by lazy { lineIcon("Moon", "M19.5 14.5A7.5 7.5 0 0 1 9.5 4.5a7.5 7.5 0 1 0 10 10z") }
    val Sun by lazy {
        lineIcon(
            "Sun",
            "M12 8.25a3.75 3.75 0 1 0 0 7.5a3.75 3.75 0 1 0 0-7.5z",
            "M12 3.5v1.5M12 19v1.5M3.5 12H5M19 12h1.5",
            "M6 6l1.05 1.05M16.95 16.95L18 18M6 18l1.05-1.05M16.95 7.05L18 6",
        )
    }
    val Device by lazy {
        lineIcon(
            "Device",
            "M7 3.75h10c.55 0 1 .45 1 1v14.5c0 .55-.45 1-1 1H7c-.55 0-1-.45-1-1V4.75c0-.55.45-1 1-1z",
            "M12 7.5v9",
            "M10.75 17.75h2.5",
        )
    }
    val Plus by lazy { lineIcon("Plus", "M12 5v14M5 12h14") }
    val Minus by lazy { lineIcon("Minus", "M5 12h14") }
    val Check by lazy { lineIcon("Check", "M5 12.5l4.5 4.5L19 7.5") }
    val Close by lazy { lineIcon("Close", "M6.5 6.5l11 11M17.5 6.5l-11 11") }
    val ChevronRight by lazy { lineIcon("ChevronRight", "M9.5 6l6 6-6 6") }
    val ChevronLeft by lazy { lineIcon("ChevronLeft", "M14.5 6l-6 6 6 6") }
    val ChevronDown by lazy { lineIcon("ChevronDown", "M6 9.5l6 6 6-6") }
    val Back by lazy { lineIcon("Back", "M19 12H5.5M11 6l-6 6 6 6") }
    val Edit by lazy {
        lineIcon(
            "Edit",
            "M4.5 19.5l1-4L15.75 5.25a1.77 1.77 0 0 1 2.5 0l.5.5a1.77 1.77 0 0 1 0 2.5L8.5 18.5z",
            "M13.5 7.5l3 3",
        )
    }
    val Trash by lazy {
        lineIcon(
            "Trash",
            "M4.5 6.5h15",
            "M9.5 6.5V4.75c0-.41.34-.75.75-.75h3.5c.41 0 .75.34.75.75V6.5",
            "M6.5 6.5l.8 12.6c.04.53.47.9 1 .9h7.4c.53 0 .96-.37 1-.9l.8-12.6",
        )
    }
    val Info by lazy {
        lineIcon("Info", "M12 3.75a8.25 8.25 0 1 0 0 16.5a8.25 8.25 0 1 0 0-16.5z", "M12 11v5", "M12 7.75v.5")
    }
    val Undo by lazy { lineIcon("Undo", "M9 14.5L4.5 10 9 5.5", "M4.5 10h10a5 5 0 0 1 0 10H11") }
}

private fun lineIcon(name: String, vararg paths: String): ImageVector {
    val builder = ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    )
    paths.forEach { data ->
        builder.addPath(
            pathData = PathParser().parsePathString(data).toNodes(),
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.75f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        )
    }
    return builder.build()
}
