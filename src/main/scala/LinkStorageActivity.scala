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

import ActionBar.OnNavigationListener

class LinkStorageActivity extends Activity 
with SectionBrowserListener with LinkBrowserListener with LinkSeqBrowserListener
with OnBackStackChangedListener {

  private var mDoc:Document = Document()
  var layoutMgr:Option[LayoutMgr] = None

  //todo: make this look up in settings.
  //and also in saved instance state.
  def docLocation:String = {
    getResources.getString(R.string.test_doc_loc)
  }

  def doc:Document = mDoc
  def doc_=(newDoc:Document):Unit = {
    mDoc = newDoc
    layoutMgr foreach (_.setDoc(mDoc))
  }

  /** Called when the activity is first created. */
  override def onCreate(savedInstanceState:Bundle) = {
    super.onCreate(savedInstanceState)
    val loc = docLocation
    getFragmentManager.addOnBackStackChangedListener(this)
    layoutMgr = Some(getLayout)
    layoutMgr map (_.prepareView) foreach (setContentView(_))
    DocLoader.load(loc, this)
  }

  def doFragTrans[U](f: FragmentTransaction => U):U = {
    val fragMan = getFragmentManager
    val fragTrans = fragMan.beginTransaction
    val res = f(fragTrans)
    fragTrans.commit
    res
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
  def establishLayout(layoutBig:Boolean, layoutSidepanes:Boolean):LayoutMgr = new SubdispSection(this) /*{ //TODO
    if (!layoutBig) establishTabLayout
    else if (!layoutSidepanes) establishVerticalLayout
    else establishPanesLayout
  }*/

  def onSectionSelect(secName:String, path:List[String]):Unit = {
    getActionBar.setDisplayHomeAsUpEnabled(true)
    val newPath = path :+ secName
    val section = (doc /~ newPath).current.asInstanceOf[Section]
    layoutMgr foreach (_.pushSection(section, newPath))
  }

  def onLinkSelect(pos:Int, path:List[String]):Unit = {
    ((doc /~ path) #@ pos) map {
      link => Log.i(TAG, link.toString)
    }
  }

  def onLinkSeqItemSelect(seq:Int, item:Int, path:List[String]):Unit = {
    ((doc /~ path) #@#@@(seq, item)) map {
      link => Log.i(TAG, link.toString)
    }
  }

  def onBackStackChanged = {
    if (getFragmentManager.getBackStackEntryCount == 0)
      getActionBar.setDisplayHomeAsUpEnabled(false)
    else { }
  }

  def goBack() = {
    getFragmentManager.popBackStack()
    layoutMgr foreach {_.popSection()}
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

  override def onBackPressed() = {
    if (getFragmentManager.getBackStackEntryCount > 0)
      goBack()
    else
      super.onBackPressed()
  }
}
