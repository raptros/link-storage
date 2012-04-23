package local.nodens.linkstorage
import android.app._
import android.os.Bundle
import android.view._
import android.widget._
import android.os.Bundle

import local.nodens.linkmodel._
/**
 * Represents a text view item that can be updated.
 */
class DisplayItem(val activity:Activity, var content:String)
extends TextView(activity) {
  setText(content)

  def update(newContent:String):DisplayItem = { 
    content = newContent
    setText(content)
    this
  }
}

/**
 * Adapter for displaying section lists.
 */
class SectionAdapter(sections:List[String], activity:Activity) extends BaseAdapter {
  val keyArray = sections.toArray

  def newDisplayItem(pos:Int):DisplayItem = {
    val di = new DisplayItem(activity, keyArray(pos))
    di.setTextSize(40) //todo put this size in a resource
    di
  }

  def getView(pos:Int, convert:View, parent:ViewGroup):View = convert match {
    case di:DisplayItem => di.update(keyArray(pos))
    case _ => newDisplayItem(pos)
  }
  def getItemId(pos:Int):Long = pos.longValue()
  def getItem(pos:Int):Object = keyArray(pos)
  def getCount:Int = keyArray.length
}

/**
 * Adapter for displaying links.
 * @todo: make this so it can show both title and url...
 */
class LinkAdapter(titles:List[String], urls:List[String], activity:Activity) extends BaseAdapter {
  val titleArr = titles.toArray
  val urlArr = urls.toArray

  def newDisplayItem(pos:Int):DisplayItem = {
    val di = new DisplayItem(activity, titleArr(pos))
    di.setTextSize(20) //todo put this size in a resource
    di
  }

  def getView(pos:Int, convert:View, parent:ViewGroup):View = convert match {
    case di:DisplayItem => di.update(titleArr(pos))
    case _ => newDisplayItem(pos)
  }
  def getItemId(pos:Int):Long = pos.longValue()
  def getItem(pos:Int):Object = titleArr(pos)
  def getCount:Int = titleArr.length
}
