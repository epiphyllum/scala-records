package records

import scala.language.experimental.macros

import scala.reflect._
import RecordConversions._

object Rec {
  /** Create a "literal record" with field value pairs [[v]]. */
  def apply(v: (String, Any)*): Rec = macro records.Macros.apply_impl

  /**
   * An extension method for converting records into case classes.
   * It is implemented as an extension to avoid collision with the record fields.
   */
  implicit class Convert[From <: Rec](val record: From) extends AnyVal {
    def to[To]: To = macro RecordConversions.to_impl[From, To]
  }
}

/**
 * Base class for all record types. The hidden `_\u200B_data*` members should only
 * be defined and referred to by macro implementations.
 * The specialized versions should be preferred in both cases. A
 * concrete implementation of R (generated by a macro) must provide
 * overrides for either `_\u200B_data` or all `_\u200B_data*` members.
 *
 * Callers may choose either of `_\u200B_data` or the specialized
 * versions, but should prefer the specialized versions whenever
 * possible to avoid boxing.
 *
 * Rather than extending this class normally, you should use the macros provided
 * by [[Macros.RecordMacros]] to tie the records to your backend. These macros
 * will implement all methods on Rec. Additionaly, they provide capabilities to
 * extend other traits (such as Serializable or a trait defined by your project)
 * and add custom fields if required.
 *
 * If you just want to create a record, use [[Rec.apply]].
 */
trait Rec {
  /** The number of fields in this record */
  def __dataCount: Int

  /** Checks whether a field having the given [[fieldName]] exists. If this
   *  method returns true, an implementation may safely call `__dataAny`.
   */
  def __dataExists(fieldName: String): Boolean

  /** Returns the value of the field [[fieldName]] as an untyped object. This is
   *  used by the equals methods. Implementations that know the target type of
   *  the field should use the `__data` method or - even better - the
   *  specialized versions of it.
   *
   *  Calling `__dataAny` for a [[fieldName]] that does not exist on this
   *  record yields undefined behavior.
   */
  def __dataAny(fieldName: String): Any

  /** Returns the value of the field [[fieldName]] with type [[T]].
   *
   *  Calling `__data` for a [[fieldName]] that does not exist or with a wrong
   *  type (other than specified at creation) yields undefined behavior.
   */
  @inline def __data[T: ClassTag](fieldName: String): T = {
    val res = classTag[T] match {
      case ClassTag.Boolean => __dataBoolean(fieldName)
      case ClassTag.Byte    => __dataByte(fieldName)
      case ClassTag.Short   => __dataShort(fieldName)
      case ClassTag.Char    => __dataChar(fieldName)
      case ClassTag.Int     => __dataInt(fieldName)
      case ClassTag.Long    => __dataLong(fieldName)
      case ClassTag.Float   => __dataFloat(fieldName)
      case ClassTag.Double  => __dataDouble(fieldName)
      case _                => __dataObj[T](fieldName)
    }
    res.asInstanceOf[T]
  }

  /** Same as `__data`, but only for types [[T]] which are not specialized */
  @inline def __dataObj[T: ClassTag](fieldName: String): T = __data(fieldName)

  /** Same as `__data`, but only for {T = Boolean} */
  @inline def __dataBoolean(fieldName: String): Boolean = __data(fieldName)

  /** Same as `__data`, but only for {T = Byte} */
  @inline def __dataByte(fieldName: String): Byte = __data(fieldName)

  /** Same as `__data`, but only for {T = Short} */
  @inline def __dataShort(fieldName: String): Short = __data(fieldName)

  /** Same as `__data`, but only for {T = Char} */
  @inline def __dataChar(fieldName: String): Char = __data(fieldName)

  /** Same as `__data`, but only for {T = Int} */
  @inline def __dataInt(fieldName: String): Int = __data(fieldName)

  /** Same as `__data`, but only for {T = Long} */
  @inline def __dataLong(fieldName: String): Long = __data(fieldName)

  /** Same as `__data`, but only for {T = Float} */
  @inline def __dataFloat(fieldName: String): Float = __data(fieldName)

  /** Same as `__data`, but only for {T = Double} */
  @inline def __dataDouble(fieldName: String): Double = __data(fieldName)
}
