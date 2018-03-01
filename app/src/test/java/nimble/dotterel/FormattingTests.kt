// This file is part of Dotterel which is released under GPL-2.0-or-later.
// See file <LICENSE.txt> or go to <http://www.gnu.org/licenses/> for full license details.

package nimble.dotterel

import io.kotlintest.matchers.shouldBe
import io.kotlintest.properties.*
import io.kotlintest.specs.FunSpec

import nimble.dotterel.translation.*

class FormattingTests : FunSpec
({
	test("spacing")
	{
		val spacingTable = table(
			headers("context", "text", "result"),
			row(Formatting.Space.NORMAL, Formatting.Space.NORMAL, " "),
			row(Formatting.Space.NORMAL, Formatting.Space.NONE, ""),
			row(Formatting.Space.NORMAL, Formatting.Space.GLUE, " "),
			row(Formatting.Space.NONE, Formatting.Space.NORMAL, ""),
			row(Formatting.Space.NONE, Formatting.Space.NONE, ""),
			row(Formatting.Space.NONE, Formatting.Space.GLUE, ""),
			row(Formatting.Space.GLUE, Formatting.Space.NORMAL, " "),
			row(Formatting.Space.GLUE, Formatting.Space.NONE, ""),
			row(Formatting.Space.GLUE, Formatting.Space.GLUE, "")
		)
		forAll(spacingTable) { a, b, result ->
			UnformattedText(0, "text", Formatting(spaceStart = b))
				.format(FormattedText(0, "context", Formatting(spaceEnd = a, space = " ")))
				.text shouldBe result + "text"
		}

		run {
			val context = FormattedText(0, "context", Formatting(space = " "))
			val a = UnformattedText(0, "text").format(context)
			val b = UnformattedText(0, "btextb").format(a)

			b.text shouldBe " btextb"
		}

		run {
			val context = FormattedText(
				0,
				"context",
				Formatting(spaceEnd = Formatting.Space.NORMAL, space = " "))
			val a = UnformattedText(
				0,
				"text",
				Formatting(spaceStart = Formatting.Space.NONE))
				.format(context)
			val b = UnformattedText(
				0,
				"btextb",
				Formatting(spaceStart = Formatting.Space.NORMAL))
				.format(a)

			b.text shouldBe " btextb"
		}

		run {
			val context = FormattedText(
				0,
				"context",
				Formatting(spaceEnd = Formatting.Space.NORMAL, space = " "))
			val a = UnformattedText(
				0,
				"text",
				Formatting(
					spaceStart = Formatting.Space.NONE,
					spaceEnd = Formatting.Space.NONE)
			).format(context)
			val b = UnformattedText(
				0,
				"btextb",
				Formatting(spaceStart = Formatting.Space.NORMAL)
			).format(a)

			b.text shouldBe "btextb"
		}
	}

	test("custom space")
	{
		run {
			val context = FormattedText(
				0,
				"context",
				Formatting(spaceEnd = Formatting.Space.NORMAL, space = "_@"))
			val a = UnformattedText(
				0,
				"text  text",
				Formatting(spaceStart = Formatting.Space.NORMAL)
			).format(context)

			a.text shouldBe "_@text_@_@text"
		}

		run {
			val context = FormattedText(
				0,
				"context",
				Formatting(spaceEnd = Formatting.Space.GLUE, space = "_@"))
			val a = UnformattedText(
				0,
				"text  text",
				Formatting(spaceStart = Formatting.Space.GLUE)
			).format(context)

			a.text shouldBe "text_@_@text"
		}
	}

	test("backspacing")
	{
		(FormattedText(10, "context") + FormattedText(6, "text"))
			.text shouldBe "ctext"

		(FormattedText(10, "context") + FormattedText(12, "text"))
			.text shouldBe "text"

		(FormattedText(10, "context") + FormattedText(12, "text"))
			.backspaces shouldBe 15
	}
})