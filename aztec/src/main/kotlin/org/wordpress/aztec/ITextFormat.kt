package org.wordpress.aztec

/**
 * Defines the text formatting type.
 *
 * It is used instead of an enumeration, so that additional plugins can define their own format types.
 * The basic format types are defined in [AztecTextFormat]. A plugin should provide its own *enum class* implementation.
 *
 * @property name the enum member name.
 */
interface ITextFormat {
    val name: String
}