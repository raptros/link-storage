package local.nodens

package object linkstorage {
  import scalaz._
  import Scalaz._
  import local.nodens.linkmodel._

  val TAG = "LinkStorageActivity"

  val kSISDocType = "sis_doc_type"
  val kSISDocFile = "sis_doc_file"
  val kSISDocID = "sis_doc_id"
  val kSISPath = "sis_path"

  val SIS_DOC_TYPE_STATIC:String = "sis_doc_type_static"
  val SIS_DOC_TYPE_EXTERN:String = "sis_doc_type_extern"

  type LoadFail = String
  type Loaded = Validation[LoadFail, Document]
  type OnLoad = Document => Unit
  type OnLoadFail = LoadFail => Unit

  import android.os.Bundle
  import scala.collection.JavaConversions._
  class StringListBundle(bundle:Bundle) {
    import java.util.{ArrayList => JArrayList}
    //extract as string list
    def extractStringList(key:String):List[String] = try {
      (bundle getStringArrayList key).toList
    } catch {
      case e:NullPointerException => Nil
    }
    //put a string iterable in (as an arraylist)
    def putStringList(key:String, iter:Iterable[String]):Unit = {
      bundle.putStringArrayList(key, new JArrayList[String](iter))
    }
  }
  implicit def bundle2StringListBundle(bundle:Bundle):StringListBundle = new StringListBundle(bundle)

  type FileFailure = String
  trait HasLSA { def activity:LinkStorageActivity }

  case class SecStackItem(
    hasSections:Boolean,
    hasLinks:Boolean,
    hasLinkSeqs:Boolean,
    doc:Document,
    path:List[String])

  object SecStackItem {
    def fromDoc(doc:Document) = new SecStackItem(
      doc.sections.isEmpty, false, false, doc, Nil)

    def fromSection(doc:Document, sec:Section, path:List[String]) = new SecStackItem(
      !sec.sections.isEmpty, !sec.links.isEmpty, !sec.linkSeqs.isEmpty,
      doc, path)

    def fromManipulator(manip:Manipulator):SecStackItem = manip.current match {
      case (doc:Document) => fromDoc(doc)
      case (sec:Section) => {
        fromSection(manip.getDoc, sec, manip.path.tail)
      }
    }
  }
}
