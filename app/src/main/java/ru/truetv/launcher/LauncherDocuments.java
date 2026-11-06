package ru.truetv.launcher;

import android.database.*;
import android.os.*;
import android.provider.*;
import java.io.*;

/** Actual SAF document access; not a FileProvider-only sharing shim. */
public final class LauncherDocuments extends DocumentsProvider {
 private FileStore files(){return FileStore.get(getContext());}
 public boolean onCreate(){return true;}
 private static final String[] ROOT={DocumentsContract.Root.COLUMN_ROOT_ID,DocumentsContract.Root.COLUMN_DOCUMENT_ID,DocumentsContract.Root.COLUMN_TITLE,DocumentsContract.Root.COLUMN_FLAGS,DocumentsContract.Root.COLUMN_MIME_TYPES,DocumentsContract.Root.COLUMN_AVAILABLE_BYTES,DocumentsContract.Root.COLUMN_ICON};
 private static final String[] DOC={DocumentsContract.Document.COLUMN_DOCUMENT_ID,DocumentsContract.Document.COLUMN_DISPLAY_NAME,DocumentsContract.Document.COLUMN_MIME_TYPE,DocumentsContract.Document.COLUMN_FLAGS,DocumentsContract.Document.COLUMN_SIZE,DocumentsContract.Document.COLUMN_LAST_MODIFIED};
 private FileNotFoundException failure(Exception e){return new FileNotFoundException(String.valueOf(e.getMessage()));}
 public Cursor queryRoots(String[] projection){MatrixCursor c=new MatrixCursor(projection==null?ROOT:projection);for(FileStore.Root r:files().roots()){MatrixCursor.RowBuilder row=c.newRow();row.add(DocumentsContract.Root.COLUMN_ROOT_ID,r.id);row.add(DocumentsContract.Root.COLUMN_DOCUMENT_ID,"root:"+r.id);row.add(DocumentsContract.Root.COLUMN_TITLE,r.title);row.add(DocumentsContract.Root.COLUMN_FLAGS,DocumentsContract.Root.FLAG_SUPPORTS_IS_CHILD|(r.file.canWrite()?DocumentsContract.Root.FLAG_SUPPORTS_CREATE:0));row.add(DocumentsContract.Root.COLUMN_MIME_TYPES,"*/*");row.add(DocumentsContract.Root.COLUMN_AVAILABLE_BYTES,r.file.getUsableSpace());row.add(DocumentsContract.Root.COLUMN_ICON,R.drawable.launcher_icon);}return c;}
 private void append(MatrixCursor c,FileStore.Node n){int flags=n.directory()?(n.file.canWrite()?DocumentsContract.Document.FLAG_DIR_SUPPORTS_CREATE:0):(n.file.canWrite()?DocumentsContract.Document.FLAG_SUPPORTS_WRITE:0);if(!n.root()&&n.file.getParentFile().canWrite())flags|=DocumentsContract.Document.FLAG_SUPPORTS_DELETE|DocumentsContract.Document.FLAG_SUPPORTS_RENAME;MatrixCursor.RowBuilder row=c.newRow();row.add(DocumentsContract.Document.COLUMN_DOCUMENT_ID,n.id);row.add(DocumentsContract.Document.COLUMN_DISPLAY_NAME,files().title(n));row.add(DocumentsContract.Document.COLUMN_MIME_TYPE,n.directory()?DocumentsContract.Document.MIME_TYPE_DIR:FileCore.mime(n.file.getName()));row.add(DocumentsContract.Document.COLUMN_FLAGS,flags);row.add(DocumentsContract.Document.COLUMN_SIZE,n.directory()?null:n.file.length());row.add(DocumentsContract.Document.COLUMN_LAST_MODIFIED,n.file.lastModified());}
 public Cursor queryDocument(String id,String[] projection)throws FileNotFoundException {try{MatrixCursor c=new MatrixCursor(projection==null?DOC:projection);append(c,files().node(id));return c;}catch(IOException e){throw failure(e);}}
 public Cursor queryChildDocuments(String id,String[] projection,String sort)throws FileNotFoundException {try{MatrixCursor c=new MatrixCursor(projection==null?DOC:projection);for(FileStore.Node n:files().children(files().node(id)))append(c,n);c.setNotificationUri(getContext().getContentResolver(),DocumentsContract.buildChildDocumentsUri(FileStore.AUTHORITY,id));return c;}catch(IOException e){throw failure(e);}}
 public ParcelFileDescriptor openDocument(String id,String mode,CancellationSignal cancel)throws FileNotFoundException {try{if(cancel!=null)cancel.throwIfCanceled();FileStore.Node n=files().node(id);if(n.directory())throw new IOException("Это папка");return ParcelFileDescriptor.open(n.file,ParcelFileDescriptor.parseMode(mode));}catch(IOException|IllegalArgumentException e){throw failure(e);}}
 public boolean isChildDocument(String parent,String child){try{FileStore.Node p=files().node(parent),n=files().node(child);return p.directory()&&FileCore.inside(p.file,n.file);}catch(IOException e){return false;}}
 public String createDocument(String parent,String mime,String name)throws FileNotFoundException {try{return files().create(files().node(parent),name,DocumentsContract.Document.MIME_TYPE_DIR.equals(mime)).id;}catch(IOException e){throw failure(e);}}
 public void deleteDocument(String id)throws FileNotFoundException {try{files().delete(files().node(id));}catch(IOException e){throw failure(e);}}
 public String renameDocument(String id,String name)throws FileNotFoundException {try{return files().rename(files().node(id),name).id;}catch(IOException e){throw failure(e);}}
}
