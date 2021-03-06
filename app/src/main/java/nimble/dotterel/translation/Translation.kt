// This file is part of Dotterel which is released under GPL-2.0-or-later.
// See file <LICENSE.txt> or go to <http://www.gnu.org/licenses/> for full license details.

package nimble.dotterel.translation

data class Translation(
	val strokes: List<Stroke>,
	val replaces: List<HistoryTranslation>,
	val raw: String,
	val isUntranslate: Boolean,
	val	hasPrefix: Boolean,
	val hasSuffix: Boolean)
