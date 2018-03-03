// This file is part of Dotterel which is released under GPL-2.0-or-later.
// See file <LICENSE.txt> or go to <http://www.gnu.org/licenses/> for full license details.

package nimble.dotterel.translation

import java.text.ParseException

private val TRANSLATION_PARTS_PATTERN = Regex("\\A(?:(?:[^{}\\\\]|(?:\\\\.))+|(?:\\{(?:[^{}\\\\]|(?:\\\\.))*\\}))")
// META_FORMATTING_PATTERN groups
// 1 - Formatting start
// 2 - No space start/glue
// 3 - Carry transform
// 4 - Text
// 5 - Formatting end
// 6 - No space end
private val META_FORMATTING_PATTERN = Regex("(((?:\\^|&)?)((?:~\\|)?))(.*[^\\^])((\\^?))")

private fun parseTranslationParts(translation: String): List<String>
{
	val parts = mutableListOf<String>()
	var i = 0
	while(true)
	{
		val match = TRANSLATION_PARTS_PATTERN.find(translation.substring(i)) ?: break
		i += match.range.endInclusive + 1
		val s = match.value.trim(' ')
		if(s.isNotEmpty())
			parts.add(s)
	}

	if(i != translation.length)
		throw ParseException("Error parsing translation", i)

	return parts
}

private val SIMPLE_META: Map<String, List<Any>> =
	{
		val simpleMeta = mutableMapOf<String, List<Any>>()

		val baseFormatting = Formatting(
			spaceStart = Formatting.Space.NONE,
			spaceEnd = null
		)

		val formattingAlias =
			{ it: Formatting -> listOf(UnformattedText(formatting = it)) }

		simpleMeta[""] = formattingAlias(baseFormatting.copy(
			transformState = Formatting.TransformState.MAIN
		))
		simpleMeta["-|"] = formattingAlias(baseFormatting.copy(
			singleTransform = ::capitialiseTransform,
			transformState = Formatting.TransformState.MAIN
		))
		simpleMeta["<"] = formattingAlias(baseFormatting.copy(
			singleTransform = ::upperCaseTransform,
			transformState = Formatting.TransformState.MAIN
		))
		simpleMeta[">"] = formattingAlias(baseFormatting.copy(
			singleTransform = ::lowerCaseTransform,
			transformState = Formatting.TransformState.MAIN
		))
		val noSpaceText = formattingAlias(Formatting(
			spaceStart = Formatting.Space.NONE,
			spaceEnd = Formatting.Space.NONE
		))
		simpleMeta["^"] = noSpaceText
		simpleMeta["^^"] = noSpaceText

		simpleMeta
	}()

data class TranslationPart(
	val actions: List<Any> = listOf(),
	val replaces: List<HistoryTranslation> = listOf())
{
	val empty: Boolean
		get() = actions.isEmpty() && replaces.isEmpty()

	operator fun plus(b: TranslationPart): TranslationPart =
		TranslationPart(this.actions + b.actions, b.replaces + this.replaces)
}

class TranslationProcessor
{
	private val simpleMeta: MutableMap<String, List<Any>> = SIMPLE_META.toMutableMap()

	private fun parseKeyCombos(keyCombosStr: String): TranslationPart =
		TranslationPart()

	private fun parseCommand(commandStr: String): TranslationPart =
		TranslationPart()

	@Throws(ParseException::class)
	private fun parseTranslationPart(translator: Translator, part: String)
		: TranslationPart
	{
		if(part[0] == '{')
		{
			val inner = part.substring(1, part.length - 1)

			val m = this.simpleMeta[inner]
			if(m != null)
				return TranslationPart(m)

			if(inner.isNotEmpty() && inner[0] == '#')
				return this.parseKeyCombos(inner.substring(1))

			val formattingMatch = META_FORMATTING_PATTERN.matchEntire(inner)
			if(formattingMatch != null)
			{
				// If no formatting tokens, it's a command
				if(formattingMatch.groupValues[1].isEmpty()
					&& formattingMatch.groupValues[5].isEmpty())
					return this.parseCommand(inner)

				var spaceStart = Formatting.Space.NORMAL
				var spaceEnd = Formatting.Space.NORMAL
				var singleTransformState = Formatting.TransformState.NORMAL

				when(formattingMatch.groupValues[2])
				{
					"^" ->
						spaceStart = Formatting.Space.NONE
					"&" ->
					{
						spaceStart = Formatting.Space.GLUE
						spaceEnd = Formatting.Space.GLUE
					}
				}

				if(formattingMatch.groupValues[3] == "~|")
					singleTransformState = Formatting.TransformState.CARRY

				val text = formattingMatch.groupValues[4]

				if(formattingMatch.groupValues[6] == "^")
					spaceEnd = Formatting.Space.NONE

				val formatting = Formatting(
					spaceStart = spaceStart,
					spaceEnd = spaceEnd,
					transformState = singleTransformState
				)

				return TranslationPart(listOf(UnformattedText(0, text, formatting)))
			}

			return TranslationPart()
		}
		else
			return TranslationPart(listOf(UnformattedText(0, part)))
	}

	fun process(translator: Translator, translation: String): TranslationPart
	{
		return parseTranslationParts(translation)
			.map({ this.parseTranslationPart(translator, it) })
			.fold(TranslationPart(), { acc, it -> acc + it })
	}
}
