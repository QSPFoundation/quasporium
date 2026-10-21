package com.pixnpunk.game.data

import java.io.ByteArrayOutputStream

/**
 * Builds a game file in the QSPGAME format both engines read: UCS-2 LE fields, each followed by
 * a line break, with the coded fields shifted by 5 (the format QGen writes).
 */
internal object TestGame {
    const val START_TEXT = "Hello"
    const val CLICKED_TEXT = "Clicked"
    const val RESTORED_TEXT = "Restored"

    /** Prints [RESTORED_TEXT] when the action of the start location has run. */
    const val CHECK_RESTORED = "*clr & if x = 42: *p '$RESTORED_TEXT'"

    val bytes: ByteArray = build(
        Location(
            name = "start",
            description = START_TEXT,
            actions = listOf(Action(name = "Click", code = "x = 42 & *clr & *p '$CLICKED_TEXT'"))
        )
    )

    data class Action(val name: String, val code: String, val image: String = "")

    data class Location(
        val name: String,
        val description: String = "",
        val code: String = "",
        val actions: List<Action> = emptyList()
    )

    fun build(vararg locations: Location): ByteArray {
        val out = ByteArrayOutputStream()

        fun field(text: String, isCoded: Boolean = true) {
            for (ch in text) {
                val unit = if (!isCoded) ch.code else if (ch.code == 5) 0x10000 - 5 else (ch.code - 5) and 0xFFFF
                out.write(unit and 0xFF)
                out.write(unit shr 8)
            }
            for (ch in "\r\n") {
                out.write(ch.code)
                out.write(0)
            }
        }

        field("QSPGAME", isCoded = false)
        field("Quasporium tests", isCoded = false)
        field("")
        field(locations.size.toString())

        for ((name1, description, code1, actions) in locations) {
            field(name1)
            field(description)
            field(code1)
            field(actions.size.toString())
            for ((name, code, image) in actions) {
                field(image)
                field(name)
                field(code)
            }
        }

        return out.toByteArray()
    }
}
