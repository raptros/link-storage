package local.nodens.linkstorage
import browser._
import layout._

import android.app._
import android.os.Bundle
import android.view._
import android.widget._
import android.content.res.Resources

import FragmentManager.OnBackStackChangedListener
import local.nodens.linkmodel._
import android.util.Log

import scala.collection.mutable.Stack
import ActionBar.OnNavigationListener


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

class LinkStorageActivity extends Activity  {

  var layoutMgr:Option[LayoutMgr] = None
  private var mDoc:Document = Document()

  private val browse = Stack.empty[SecStackItem]

  def current = browse.headOption getOrElse(SecStackItem.fromDoc(mDoc))


  //todo: make this look up in settings.
  //and also in saved instance state.
  def loadDoc():Unit = {
    val localDoc = getResources.getString(R.string.test_doc_loc)
    val staticDoc = R.raw.testfile
    DocLoader.loadRaw(staticDoc, this)
  }

  def doc:Document = mDoc
  def doc_=(newDoc:Document):Unit = {
    mDoc = newDoc
    browse.clear()
    layoutMgr foreach (_.display(current))
  }

  /** Called when the activity is first created. */
  override def onCreate(savedInstanceState:Bundle) = {
    super.onCreate(savedInstanceState)
    //val loc = docLocation
    //getFragmentManager.addOnBackStackChangedListener(this)
    layoutMgr = Some(getLayout)
    layoutMgr map (_.prepareView) foreach (setContentView(_))
    loadDoc()
  }

  def pushSection(path:List[String]) = {
    getActionBar.setDisplayHomeAsUpEnabled(true)
    browse.push(SecStackItem.fromManipulator(doc /~ path))
    layoutMgr foreach (_.display(current))
  }

  def getLayout:LayoutMgr = {
    val resources = getResources
    val layoutBig = resources.getBoolean(R.bool.layout_big)
    val layoutSidepanes = resources.getBoolean(R.bool.layout_sidepanes)
    establishLayout(layoutBig, layoutSidepanes)
  }

  /**
   * create instance of the appropriate layout.
   */
  def establishLayout(layoutBig:Boolean, layoutSidepanes:Boolean):LayoutMgr = new TabLayout2(this) /*{ //TODO
    if (!layoutBig) establishTabLayout
    else if (!layoutSidepanes) establishVerticalLayout
    else establishPanesLayout
  }*/

  def onSectionSelect(secName:String):Unit = {
    Log.d(TAG, "on section select: " + secName + " in " + current.path.toString)
    pushSection(current.path :+ secName)
  }

  def onLinkSelect(pos:Int):Unit = {
    ((doc /~ current.path) #@ pos) map {
      link => Log.i(TAG, link.toString)
    }
  }

  def onLinkSeqItemSelect(seq:Int, item:Int):Unit = {
    ((doc /~ current.path) #@#@@(seq, item)) map {
      link => Log.i(TAG, link.toString)
    }
  }

  def goBack() = if (browse nonEmpty) {
    browse.pop()
    layoutMgr foreach (_.display(current))
    if (browse nonEmpty) { }
    else getActionBar.setDisplayHomeAsUpEnabled(false)
  }

  override def onOptionsItemSelected(item:MenuItem) = {
    item.getItemId match {
      case android.R.id.home => {
        goBack()
        true
      }
      case _ => super.onOptionsItemSelected(item)
    }
  }

  override def onBackPressed() = if (browse nonEmpty) goBack() else super.onBackPressed()
}
