package local.nodens.linkstorage.layout
import local.nodens.linkstorage._

import android.app._
import android.os.Bundle
import android.view._
import android.widget._
import android.util.{Log, TypedValue}
import local.nodens.linkmodel._
import android.content._
import res.Resources

import TypedValue.COMPLEX_UNIT_DIP
import LinearLayout.LayoutParams
import ViewGroup.LayoutParams.{MATCH_PARENT, WRAP_CONTENT}
import Gravity.{LEFT, RIGHT, CENTER_HORIZONTAL, CENTER_VERTICAL}
import ActionBar.{
  NAVIGATION_MODE_TABS,
  NAVIGATION_MODE_STANDARD,
  DISPLAY_SHOW_HOME,
  DISPLAY_HOME_AS_UP
}
import View.OnClickListener

/**
 * Control Fragment is a way to access the links and link sequences in the Subdisp layout.
 */
class ControlFragment(
  val linkCallback:OnClickListener,
  val lsCallback:OnClickListener)
extends Fragment {

  var linkEnable:Boolean = false
  var lsEnable:Boolean = false

  override def onCreateView(infl:LayoutInflater, container:ViewGroup, sis:Bundle):View = {
    val ll = infl.inflate(R.layout.control_fragment_layout, null).asInstanceOf[LinearLayout]
    def prepareButton(id:Int, callback:OnClickListener, enabled:Boolean) = getButtonFromView(id, ll) foreach {
      button => {
        button.setOnClickListener(callback)
        button.setEnabled(enabled)
      }
    }
    List((R.id.control_button_links, linkCallback, linkEnable), 
      (R.id.control_button_linkseqs, lsCallback, lsEnable)) foreach {
      case (id, callback, enabled) => prepareButton(id, callback, enabled)
    }
    ll
  }

  
  def optionView:Option[View] = {
    val v = getView
    if (v == null) None else Some(v)
  }

  def getButtonFromView(id:Int, view:View) = {
    val v = view.findViewById(id)
    if (v == null || !v.isInstanceOf[Button]) None else Some(v.asInstanceOf[Button])
  }
  
  def getButton(id:Int):Option[Button] = optionView flatMap (getButtonFromView(id, _))

  def hasContent(links:Boolean, linkSeqs:Boolean):Unit = {
    linkEnable = links
    lsEnable = linkSeqs
    getButton(R.id.control_button_links) foreach (_.setEnabled(links))
    getButton(R.id.control_button_linkseqs) foreach (_.setEnabled(linkSeqs))
  }
}

/**
 * A simple label fragment to replace the control fragment when the links or
 * link sequences are being examined.
 */
class LabelFrag(val context:Context, val callback:OnClickListener, val text:String) extends Fragment {
  override def onCreateView(infl:LayoutInflater, container:ViewGroup, sis:Bundle):View = {
    val button = infl.inflate(R.layout.label_frag_view, null).asInstanceOf[Button]
    button.setText(text)
    button.setOnClickListener(callback)
    button
  }
}

/**
 * A layout that places the section on the screen, and provides access to the
 * links and link seqs using buttons on the bottom of the screen.
 */
class SubdispSection(activity:LinkStorageActivity) extends LayoutMgr(activity) {
  val browseID:Int = R.id.subdisp_browse
  val controlID:Int = R.id.subdisp_control
  val inflater = LayoutInflater.from(activity)
  val fragMan = activity.getFragmentManager

  object LinkListener extends OnClickListener { def onClick(v:View) =  pushSubFrag(oLinkFrag, linkLabelFrag) }
  object LSListener extends OnClickListener { def onClick(v:View) =  pushSubFrag(oLSFrag, lsLabelFrag) }
  object GoBackListener extends OnClickListener { def onClick(v:View) = activity.goBack() }

  val controlFrag = new ControlFragment(LinkListener, LSListener)
  val linkLabelFrag = new LabelFrag(activity, GoBackListener, "Links")
  val lsLabelFrag = new LabelFrag(activity, GoBackListener, "Link Seqs")

  var oLinkFrag:Option[Fragment] = None
  var oLSFrag:Option[Fragment] = None


  def pushSubFrag(oFrag:Option[Fragment], newFrag:Fragment):Unit = { 
    oFrag foreach {
      frag => activity.doFragTrans {
        ft => {
          ft.replace(browseID, frag)
          ft.replace(controlID, newFrag)
          ft.addToBackStack(null)
        }
      }
    }
  }

  def prepareView:View = inflater.inflate(R.layout.subdisp_layout, null).asInstanceOf[LinearLayout]

  def addDoc(docFrag:Fragment):Unit = activity.doFragTrans (_.add(browseID, docFrag))

  def ensureFrag(ft:FragmentTransaction, frag:Fragment, id:Int) = {
    if (frag.isAdded) {
      if (frag.isDetached) ft.attach(frag) else { }
    } else {
      val present = fragMan.findFragmentById(id)
      if (present == null) { } else ft.remove(present)
      ft.add(id, frag)
    }
  }

  def addAll(secFrag:Fragment, linkFrag:Fragment, lsFrag:Fragment):Unit = {
    oLinkFrag = Some(linkFrag)
    oLSFrag = Some(lsFrag)
    activity.doFragTrans {
      ft => {
        ensureFrag(ft, secFrag, browseID)
        ensureFrag(ft, controlFrag, controlID)
        ft.addToBackStack(null)
      }
    }
  }

  def sectionContentsAre(links:Boolean, linkSeqs:Boolean):Unit = controlFrag.hasContent(links, linkSeqs)
}

