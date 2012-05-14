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

@EnhanceStrings
class LinkStorageActivity extends Activity {
  val loader = new DocLocLoader(this)

  var layoutMgr:Option[LayoutMgr] = None
  private var mDoc:Document = Document()
  private var docLoc:DocLocation = NoLoadedDoc

  private val browse = Stack.empty[SecStackItem]

  def current = browse.headOption getOrElse(SecStackItem.fromDoc(mDoc))

  def onLoadFail(failure:LoadFail):Unit = Log.e(TAG, failure)

  def loadDoc(loc:DocLocation, path:List[String], sis:Option[Bundle] = None):Unit = loader.load(loc) {
    lDoc => { 
      docLoc = loc //only store to docLoc if load was successful
      doc = lDoc
      path foreach (onSectionSelect(_))
      layoutMgr foreach (_.restoreInstanceState(sis))
    }
  }(onLoadFail(_))

  def doc:Document = mDoc
  def doc_=(newDoc:Document):Unit = { mDoc = newDoc;  browse.clear(); layoutMgr foreach (_.display(current)) }
  
  def loadFilePref():Option[(DocLocation, List[String])] = None

  /** Called when the activity is first created. */
  override def onCreate(savedInstanceState:Bundle) = {
    super.onCreate(savedInstanceState)
    val default = (StaticDoc(R.raw.testfile), Nil)
    val sis = Option(savedInstanceState) 
    val (loc, path) = sis map (extractSIS _) orElse loadFilePref() getOrElse (default)
    Log.d(TAG, "onCreate extract loc: #loc.toString and path #path.toString")
    layoutMgr = Some(getLayout)
    layoutMgr map (_.prepareView) foreach (setContentView(_))
    loadDoc(loc, path, sis)
  }
  
  override def onResume() = {
    super.onRestart()
    layoutMgr foreach (_.display(current))
  }

  def extractSIS(sis:Bundle):(DocLocation, List[String]) = {
    val loc = (sis getString kSISDocType) match {
      case SIS_DOC_TYPE_STATIC => StaticDoc(sis.getInt(kSISDocID))
      case SIS_DOC_TYPE_EXTERN => ExternalDoc(sis.getString(kSISDocFile))
      case _ => NoLoadedDoc
    }
    val path = sis extractStringList kSISPath
    (loc, path)
  }
  
  override def onSaveInstanceState(outSIS:Bundle):Unit = {
    //super.onSaveInstanceState(outSIS)
    layoutMgr foreach (_.onSaveInstanceState(outSIS))
    docLoc match {
      case StaticDoc(id) => {
        outSIS.putInt(kSISDocID, id)
        outSIS.putString(kSISDocType, SIS_DOC_TYPE_STATIC)
      }
      case ExternalDoc(path) => {
        outSIS.putString(kSISDocFile, path)
        outSIS.putString(kSISDocType, SIS_DOC_TYPE_EXTERN)
      }
      case NoLoadedDoc => { }
    }
    outSIS.putStringList(kSISPath, current.path)
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
