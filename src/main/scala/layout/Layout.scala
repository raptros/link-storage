package local.nodens.linkstorage.layout
import local.nodens.linkstorage._
import browser.{SectionBrowser, LinkBrowser, LinkSeqBrowser}

import android.app._
import android.os.Bundle
import android.view._
import android.widget._
import android.util.Log
import local.nodens.linkmodel._
import android.content._

import scala.collection.mutable.Stack

class SecStackItem(
  val hasSections:Boolean,
  val hasLinks:Boolean,
  val hasLinkSeqs:Boolean,
  val doc:Document,
  val path:List[String],
  val oDoc:Option[Fragment],
  val oSec:Option[Fragment],
  val oLinks:Option[Fragment],
  val oLS:Option[Fragment])

object SecStackItem {
  def fromDoc(doc:Document) = new SecStackItem(
    doc.sections.isEmpty, false, false,
    doc, Nil, Some(SectionBrowser(doc)),
    None, None, None)

  def fromSection(doc:Document, sec:Section, path:List[String]) = new SecStackItem(
    !sec.sections.isEmpty, !sec.links.isEmpty, !sec.linkSeqs.isEmpty,
    doc, path, None,
    Some(SectionBrowser(sec, path)),
    Some(LinkBrowser(sec, path)),
    Some(LinkSeqBrowser(sec, path)))
}

/**
 */
abstract class LayoutMgr(val activity:LinkStorageActivity) {
  val actionBar = activity.getActionBar
  val inflater = LayoutInflater.from(activity)
  val fragMan = activity.getFragmentManager

  val browse = Stack.empty[SecStackItem]
  var oDoc:Option[Document] = None

  def setDoc(doc:Document):Unit = {
    browse.push(SecStackItem.fromDoc(doc))
    addDoc(current.oDoc.get)
  }

  //because of the way the backstack is set up,
  //it will go empty before the base of browse is popped.
  def current:SecStackItem = browse top

  def pushSection(section:Section, path:List[String]):Unit = {
    browse.push(SecStackItem.fromSection(current.doc, section, path))
    addAll(current.oSec.get, current.oLinks.get, current.oLS.get)
    examineCurrent()
  }

  def popSection():Unit = {
    browse.pop()
    examineCurrent()
  }

  def ensureFrag(ft:FragmentTransaction, frag:Fragment, id:Int) = {
    if (frag.isAdded) {
      if (frag.isDetached) ft.attach(frag) else { }
    } else {
      val present = fragMan.findFragmentById(id)
      if (present == null) { } else ft.remove(present)
      ft.add(id, frag)
    }
  }

  def examineCurrent():Unit
  def prepareView:View
  def addDoc(docFrag:Fragment):Unit
  def addAll(secFrag:Fragment, linkFrag:Fragment, lsFrag:Fragment):Unit
}

