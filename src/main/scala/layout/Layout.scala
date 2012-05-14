package local.nodens.linkstorage.layout
import local.nodens.linkstorage._

import android.app._
import android.os.Bundle
import android.view._
import android.widget._
import android.util.Log
import local.nodens.linkmodel._
import android.content._

import scala.collection.mutable.Stack

/**
 */
abstract class LayoutMgr(val activity:LinkStorageActivity) {
  val actionBar = activity.getActionBar
  val inflater = LayoutInflater.from(activity)
  val fragMan = activity.getFragmentManager

  def current = activity.current

  def display(sec:SecStackItem):Unit = sec match {
    case SecStackItem(hasSections, hasLinks, hasLinkSeqs, doc, Nil) => displayDoc(doc)
    case SecStackItem(hasSections, hasLinks, hasLinkSeqs, doc, path) => displaySection(sec)
  }


  def prepareView:View
  def displayDoc(doc:Document):Unit
  def displaySection(sec:SecStackItem):Unit
  def restoreInstanceState(sis:Option[Bundle]):Unit
  def onSaveInstanceState(outSIS:Bundle):Unit
}

