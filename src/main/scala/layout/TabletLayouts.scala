package local.nodens.linkstorage.layout
import local.nodens.linkstorage._
import data._

import android.app._
import android.os.Bundle
import android.view._
import android.widget._
import android.util.Log
import local.nodens.linkmodel._
import android.content._
import ActionBar.{Tab, TabListener}
import AdapterView.OnItemClickListener

import scalaz._
import Scalaz._

abstract class Large(activity:LinkStorageActivity) extends LayoutMgr(activity) {
  //the listener for items being clicked in the list 
  def makeListener[A <: ListAdapter](adapter:A)(callback: (A, Int) => Unit) = new OnItemClickListener {
    def onItemClick(parent:AdapterView[_], view:View, position:Int, id:Long) = callback(adapter, position)
  }

  def splitListener[A <: ListAdapter, B <: ListAdapter](adapter:SplitAdapter[A, B])(ucb: (A, Int) => Unit)(lcb: (B, Int) => Unit) = new SplitAdapterListener(adapter, ucb, lcb)

  def prepareView:View = {
    LayoutInflater.from(activity).inflate(layoutId, null)
  }

  def populateSecView(sections:Iterable[String]):Unit = {
    val secView = activity.findViewById(secViewId).asInstanceOf[ListView]
    val adapter = new AdaptSection(activity, sections)
    val listener = makeListener[AdaptSection](adapter) {
      (adapter, pos) => activity.onSectionSelect(adapter.keyArray(pos))
    }
    secView.setOnItemClickListener(listener)
    secView.setAdapter(adapter)
  }

  def populateLLSView(links:Seq[Link], linkSeqs:Seq[LinkSeq]):Unit = {
    val llsView = activity.findViewById(llsViewId).asInstanceOf[ListView]
    val adapter = new SplitAdapter(
      LLSHeaderItem(activity, "Links"), new AdaptLink(activity, links),
      LLSHeaderItem(activity, "Link Seqs"), new AdaptLinkSeq(activity, linkSeqs))
    val listener = splitListener(adapter) { (adapter, pos) => activity.onLinkSelect(pos) } {
      (adapter, pos) => {
        val seq = adapter.getAdapterPos(pos)
        val idx = adapter.getSubAdapterPosition(seq, pos)
        activity.onLinkSeqItemSelect(idx, seq)
      }
    }
    llsView.setOnItemClickListener(listener)
    llsView.setAdapter(adapter)
  }

  def displayDoc(doc:Document):Unit = {
    populateSecView(doc.sections.keys) 
    populateLLSView(Nil, Nil)
  }
  def displaySection(ssi:SecStackItem):Unit = {
    val sec = (ssi.doc /~ ssi.path).current.asInstanceOf[Section]
    populateSecView(sec.sections.keys)
    populateLLSView(sec.links, sec.linkSeqs)
  }

  def restoreInstanceState(sis:Option[Bundle]):Unit = { }
  def onSaveInstanceState(outSIS:Bundle):Unit = { }

  def layoutId:Int //resource id for the layout
  def secViewId:Int //view id for the section listview
  def llsViewId:Int //view id for the links/link seqs list view
}

class VerticalLarge(activity:LinkStorageActivity) extends Large(activity) {
  val layoutId = R.layout.vertical_large_layout
  val secViewId = R.id.vertical_sec_area
  val llsViewId = R.id.vertical_lls_area
}

class HorizontalLarge(activity:LinkStorageActivity) extends Large(activity) {
  val layoutId = R.layout.horizontal_large_layout
  val secViewId = R.id.horizontal_sec_area
  val llsViewId = R.id.horizontal_lls_area
}
