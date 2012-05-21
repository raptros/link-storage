package local.nodens.linkstorage.files
import local.nodens.linkstorage._
import local.nodens.linkmodel._

import android.app._
import android.content.Context
import android.os.Bundle
import android.view._
import android.widget._
import android.content.res.Resources
import android.util.Log

import scalaz._
import Scalaz._

import View.OnClickListener
import AdapterView.OnItemClickListener
import FragmentManager.OnBackStackChangedListener

object OnClickButton {
  class CallbackButton(button:Button) {
    def setCallback(cb: View => Unit):Unit = {
      val listener = new OnClickListener { def onClick(v:View) = cb(v) }
      button.setOnClickListener(listener)
    }
  }
  implicit def button2CallbackButton(b:Button) = new CallbackButton(b)
}

trait FileDialogComps {
  def mgr:FileMgr
  def prepFileList(lv:ListView, context:Context)(onFileSelect: String => Unit):Unit = {
    val adapter = new ArrayAdapter[String](context, R.layout.file_list_item)
    val listener = new OnItemClickListener {
      def onItemClick(parent:AdapterView[_], view:View, position:Int, id:Long) = adapter.getItem(position) |> onFileSelect
    }
    mgr.files |>| (adapter.add _)
    Log.d(TAG, mgr.files.toString)
    lv.setAdapter(adapter)
    lv.setOnItemClickListener(listener)
  }
}

class SaveFileAs(val activity:LinkStorageActivity) extends DialogFragment with FileDialogComps with HasLSA {
  import OnClickButton._
  val mgr = activity.fileMgr
  override def onCreateView(infl:LayoutInflater, container:ViewGroup, sis:Bundle):View = {
    val v = infl.inflate(R.layout.file_save_as, container)
    val files = v.findViewById(R.id.file_save_as_files).asInstanceOf[ListView]
    val filename = v.findViewById(R.id.file_save_as_filename).asInstanceOf[EditText]
    val cancel = v.findViewById(R.id.file_save_as_cancel).asInstanceOf[Button]
    val ok = v.findViewById(R.id.file_save_as_ok).asInstanceOf[Button]
    prepFileList(files, activity) {
      fname => filename.setText(fname)
    }
    cancel.setCallback { (v:View) => dismiss }
    ok.setCallback { 
      (v:View) => {mgr.doSaveFile(activity.doc)(filename.getText.toString); dismiss }
    }
    Option(getDialog) |>| (_.setTitle("Save as"))
    v
  }
}

class LoadFile(val activity:LinkStorageActivity) extends DialogFragment with FileDialogComps {
  import OnClickButton._
  val mgr = activity.fileMgr
  override def onCreateView(infl:LayoutInflater, container:ViewGroup, sis:Bundle):View = {
    val v = infl.inflate(R.layout.file_load, container)
    val files = v.findViewById(R.id.file_load_files).asInstanceOf[ListView]
    val cancel = v.findViewById(R.id.file_load_cancel).asInstanceOf[Button]
    val onFile = (fname:String) => {
      mgr.doLoadFile(fname)(activity.enterDoc(_, Nil))
      dismiss
    }
    onFile |> prepFileList(files, activity) 
    cancel.setCallback { (v:View) => dismiss }
    Option(getDialog) |>| (_.setTitle("Open file"))
    v
  }
}
