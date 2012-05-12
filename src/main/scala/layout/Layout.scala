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
/**
 * When one of these is created, it will set up the layout of the activity.
 * @todo implement callbacks for section browser fragment to push to new sections.


 ok, new plan. the subclasses create a view heirarchy with slots for each of the three fragments.
 This class will interact with the activity to carry out the fragment transaction;
 it will pass the fragment and transaction to the subclass, telling it to deal with it,
 then commit once done.

 it will add into the view heirarchy using specified ids.

 */
abstract class LayoutMgr(val activity:LinkStorageActivity) 
extends Fragment {
  val actionBar = activity.getActionBar
  val paths = Stack.empty[List[String]]
  var oDoc:Option[Document] = None

  def setDoc(doc:Document):Unit = {
    addDoc(SectionBrowser(doc))
    oDoc = Some(doc)
  }

  def pushSection(section:Section, path:List[String]):Unit = {
    paths.push(path)
    addAll(SectionBrowser(section, path),
      LinkBrowser(section, path),
      LinkSeqBrowser(section, path))
    examineSection(section)
  }

  def popSection():Unit = {
    paths.lastOption.flatMap {
      top => oDoc map (_ /~ (paths top))
    } foreach {
      case (sec:Section) => examineSection(sec)
      case _ => { }
    }
  }

  def examineSection(section:Section):Unit = {
    val links = !section.links.isEmpty
    val linkSeqs = !section.linkSeqs.isEmpty
    Log.d(TAG, "links: " + links + " linkSeqs: " + linkSeqs)
    sectionContentsAre(links, linkSeqs)
  }

  def prepareView:View
  def addDoc(docFrag:Fragment):Unit
  def addAll(secFrag:Fragment, linkFrag:Fragment, lsFrag:Fragment):Unit
  def sectionContentsAre(links:Boolean, linkSeqs:Boolean):Unit

}

