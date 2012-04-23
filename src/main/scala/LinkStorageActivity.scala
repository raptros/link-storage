package local.nodens.linkstorage
import android.app._
import android.os.Bundle
import android.view._
import android.widget._
import android.os.Bundle

import FragmentManager.OnBackStackChangedListener
import local.nodens.linkmodel._
import android.util.Log

import ActionBar.OnNavigationListener

class LinkStorageActivity extends Activity with SectionBrowserListener 
with LinkBrowserListener with OnBackStackChangedListener {
  var doc:Document = Document()
  var layoutMgr:Option[LayoutMgr] = None

  /** Called when the activity is first created. */
  override def onCreate(savedInstanceState:Bundle) = {
    super.onCreate(savedInstanceState)
    //setContentView(R.layout.main) // let's avoid using this if possible. it's a PITA
    getFragmentManager.addOnBackStackChangedListener(this)
    layoutMgr = Some(getLayout)
    //TODO get the document from somewhere.
    doc = Document(Section("one", ("link1" #@# "http://example.com/")))
    layoutMgr map (_.setup(doc, Nil))
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
  def establishLayout(layoutBig:Boolean, layoutSidepanes:Boolean):LayoutMgr = new TabbedSection(this) /*{ //TODO
    if (!layoutBig) establishTabLayout
    else if (!layoutSidepanes) establishVerticalLayout
    else establishPanesLayout
  }*/

  //sets up a tab-based layout
  def establishTabLayout:LayoutMgr = {
    new TabbedSection(this) 
  }
  def establishVerticalLayout ={  }
  def establishPanesLayout = {  }

  def onSectionSelect(secName:String, path:List[String]):Unit = {
    doFragTrans {
      ft => {
        ft.detach(getFragmentManager.findFragmentById(android.R.id.content))
        ft.addToBackStack(null)
      }
    }
    layoutMgr map (_.setup(doc, path :+ secName))
  }

  def onLinkSelect(pos:Int, path:List[String]):Unit = {
    ((doc /~ path) #@ pos) map {
      link => Log.i(TAG, link.toString)
    }
  }

  def onBackStackChanged = {
    if (getFragmentManager.getBackStackEntryCount == 0)
      layoutMgr map (_ tearDown)
    else {
      getActionBar.setSelectedNavigationItem(
        getFragmentManager.findFragmentById(android.R.id.content) match {
          case frag:SectionBrowser => 0
          case frag:LinkBrowser => 1
          case frag:LinkSeqBrowser => 2
        }
      )
    }
  }

  override def onOptionsItemSelected(item:MenuItem) = {
    item.getItemId match {
      case android.R.id.home => {
        getFragmentManager.popBackStack
        getFragmentManager.popBackStack
        true
      }
      case _ => super.onOptionsItemSelected(item)
    }
  }

  override def onBackPressed = {
    getFragmentManager.popBackStack
    getFragmentManager.popBackStack
  }
}
