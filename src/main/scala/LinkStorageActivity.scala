package local.nodens.linkstorage
import browser._
import layout._
import files._

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

import scalaz._
import Scalaz._

@EnhanceStrings
class LinkStorageActivity extends Activity {
  val fileMgr = new FileMgr(this)

  var layoutMgr:Option[LayoutMgr] = None
  private var mDoc:Document = Document()

  private val browse = Stack.empty[SecStackItem]

  def current = browse.headOption getOrElse(SecStackItem.fromDoc(mDoc))
  def doc:Document = mDoc
  def doc_=(newDoc:Document):Unit = enterDoc(newDoc, Nil)
  
  def enterDoc(nDoc:Document, path:List[String]) = {
    mDoc = nDoc
    enter(path)
  }

  def enter(path:List[String]) = {
    browse.clear()
    path.scanLeft(Nil:List[String])(_ :+ _) foreach {
      p => browse.push(SecStackItem.fromManipulator(mDoc /~ p))
    }
    getActionBar.setDisplayHomeAsUpEnabled(browse isEmpty)
    layoutMgr foreach (_.display(current))
  }
  
  /** Called when the activity is first created. */
  override def onCreate(savedInstanceState:Bundle) = {
    super.onCreate(savedInstanceState)
    val onSIS = (sis:Bundle) => {
      val path = sis extractStringList kSISPath
      fileMgr.restoreInstanceDoc(sis)(enterDoc(_, path))
    }
    layoutMgr = Some(getLayout)
    layoutMgr map (_.prepareView) foreach (setContentView(_))
    Option(savedInstanceState) |>| onSIS
  }
  
  override def onResume() = {
    super.onRestart()
    layoutMgr foreach (_.display(current))
  }
  
  override def onSaveInstanceState(outSIS:Bundle):Unit = {
    //super.onSaveInstanceState(outSIS)
    fileMgr.onSaveInstanceState(outSIS)
    layoutMgr foreach (_.onSaveInstanceState(outSIS))
  }

  def pushSection(path:List[String]) = {
    getActionBar.setDisplayHomeAsUpEnabled(true)
    browse.push(SecStackItem.fromManipulator(doc /~ path))
    layoutMgr foreach (_.display(current))
  }

  def getLayout:LayoutMgr = {
    val resources = getResources
    val layoutBig = resources.getBoolean(R.bool.layout_big)
    val layoutHorizontal = resources.getBoolean(R.bool.layout_horizontal)
    establishLayout(layoutBig, layoutHorizontal)
  }

  /**
   * create instance of the appropriate layout.
   */
  def establishLayout(layoutBig:Boolean, layoutHorizontal:Boolean):LayoutMgr = {
    if (!layoutBig) 
      new TabLayout2(this) 
    else if (!layoutHorizontal) 
      new VerticalLarge(this) 
    else 
      new HorizontalLarge(this)
  }

  def onSectionSelect(secName:String):Unit = {
    Log.d(TAG, "on section select: #secName in #current.path.toString")
    pushSection(current.path :+ secName)
  }

  def onLinkSelect(pos:Int):Unit = {
    ((doc /~ current.path) #@ pos) foreach {
      link => Log.i(TAG, "selected link at #pos: #link.toString" )
    }
  }

  def onLinkSeqItemSelect(item:Int, seq:Int):Unit = {
    ((doc /~ current.path) #@#@@(item, seq)) foreach {
      link => Log.i(TAG, "selected in #seq item #item: #link.toString")
    }
  }

  def goBack() = if (browse nonEmpty) {
    browse.pop()
    layoutMgr foreach (_.display(current))
    if (browse nonEmpty) { }
    else getActionBar.setDisplayHomeAsUpEnabled(false)
  }

  private val menuDispatch:PartialFunction[Int, Unit] = {
    case android.R.id.home => goBack()
    case R.id.menu_new => enterDoc(Document(), Nil)
    case R.id.menu_load => (new LoadFile(this)).show(getFragmentManager, "load")
    case R.id.menu_save => fileMgr.saveNow(doc)
    case R.id.menu_save_as => (new SaveFileAs(this)).show(getFragmentManager, "saveas")
    //todo change this to actual helpfile
    case R.id.menu_help => fileMgr.doLoadRaw(R.raw.testfile)(enterDoc(_, Nil))
  }
  override def onOptionsItemSelected(item:MenuItem) = (menuDispatch lift item.getItemId) ? true | super.onOptionsItemSelected(item)

  override def onCreateOptionsMenu(menu:Menu):Boolean = {
    getMenuInflater.inflate(R.menu.link_storage_activity_menu, menu)
    true
  }

  override def onBackPressed() = if (browse nonEmpty) goBack() else super.onBackPressed()
}
