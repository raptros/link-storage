package local.nodens.linkstorage
import android.app._
import local.nodens.linkmodel._

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

sealed abstract trait DocLocation 
case object NoLoadedDoc extends DocLocation
case class StaticDoc(id:Int) extends DocLocation
case class ExternalDoc(path:String) extends DocLocation

class DocLocLoader(val activity:Activity) {
  import scala.runtime.RichInt

  def load(docLoc:DocLocation) = docLoc match {
    case NoLoadedDoc => loadNone _
    case StaticDoc(id) => loadStatic(id) _
    case ExternalDoc(path) => loadExternal(path) _
  }
  def loadNone(loaded:OnLoad)(failed:OnLoadFail) = { }
  def loadStatic(id:Int)(loaded:OnLoad)(failed:OnLoadFail) = {
    val dl = new RawDocLoader(activity.getResources, loaded, failed)
    dl.execute(new RichInt(id))
  }
  def loadExternal(path:String)(loaded:OnLoad)(failed:OnLoadFail) = {
    val dl = new DocLoader(loaded, failed)
    dl.execute(path)
  }
}

