package local.nodens

package object linkstorage {
  val TAG = "LinkStorageActivity"

  def nullToOption[A](nullable:A):Option[A] = if (nullable == null) None else Some(nullable)
}
