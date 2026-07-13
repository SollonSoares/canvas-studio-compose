package com.canvasstudio.data.local.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.canvasstudio.data.local.entity.BlockEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class BlockDao_Impl implements BlockDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<BlockEntity> __insertionAdapterOfBlockEntity;

  private final EntityDeletionOrUpdateAdapter<BlockEntity> __deletionAdapterOfBlockEntity;

  private final EntityDeletionOrUpdateAdapter<BlockEntity> __updateAdapterOfBlockEntity;

  private final SharedSQLiteStatement __preparedStmtOfClearCanvas;

  public BlockDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfBlockEntity = new EntityInsertionAdapter<BlockEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `canvas_blocks` (`id`,`projectId`,`title`,`type`,`posX`,`posY`,`width`,`height`,`contentJson`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final BlockEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getProjectId());
        if (entity.getTitle() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getTitle());
        }
        if (entity.getType() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getType());
        }
        statement.bindDouble(5, entity.getPosX());
        statement.bindDouble(6, entity.getPosY());
        statement.bindLong(7, entity.getWidth());
        statement.bindLong(8, entity.getHeight());
        if (entity.getContentJson() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getContentJson());
        }
      }
    };
    this.__deletionAdapterOfBlockEntity = new EntityDeletionOrUpdateAdapter<BlockEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `canvas_blocks` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final BlockEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfBlockEntity = new EntityDeletionOrUpdateAdapter<BlockEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `canvas_blocks` SET `id` = ?,`projectId` = ?,`title` = ?,`type` = ?,`posX` = ?,`posY` = ?,`width` = ?,`height` = ?,`contentJson` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final BlockEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getProjectId());
        if (entity.getTitle() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getTitle());
        }
        if (entity.getType() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getType());
        }
        statement.bindDouble(5, entity.getPosX());
        statement.bindDouble(6, entity.getPosY());
        statement.bindLong(7, entity.getWidth());
        statement.bindLong(8, entity.getHeight());
        if (entity.getContentJson() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getContentJson());
        }
        statement.bindLong(10, entity.getId());
      }
    };
    this.__preparedStmtOfClearCanvas = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM canvas_blocks WHERE projectId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertBlock(final BlockEntity block, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfBlockEntity.insertAndReturnId(block);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteBlock(final BlockEntity block, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfBlockEntity.handle(block);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateBlock(final BlockEntity block, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfBlockEntity.handle(block);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object clearCanvas(final long projectId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearCanvas.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, projectId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearCanvas.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<BlockEntity>> getBlocksForProject(final long projectId) {
    final String _sql = "SELECT * FROM canvas_blocks WHERE projectId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, projectId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"canvas_blocks"}, new Callable<List<BlockEntity>>() {
      @Override
      @NonNull
      public List<BlockEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfProjectId = CursorUtil.getColumnIndexOrThrow(_cursor, "projectId");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfPosX = CursorUtil.getColumnIndexOrThrow(_cursor, "posX");
          final int _cursorIndexOfPosY = CursorUtil.getColumnIndexOrThrow(_cursor, "posY");
          final int _cursorIndexOfWidth = CursorUtil.getColumnIndexOrThrow(_cursor, "width");
          final int _cursorIndexOfHeight = CursorUtil.getColumnIndexOrThrow(_cursor, "height");
          final int _cursorIndexOfContentJson = CursorUtil.getColumnIndexOrThrow(_cursor, "contentJson");
          final List<BlockEntity> _result = new ArrayList<BlockEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final BlockEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpProjectId;
            _tmpProjectId = _cursor.getLong(_cursorIndexOfProjectId);
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpType;
            if (_cursor.isNull(_cursorIndexOfType)) {
              _tmpType = null;
            } else {
              _tmpType = _cursor.getString(_cursorIndexOfType);
            }
            final float _tmpPosX;
            _tmpPosX = _cursor.getFloat(_cursorIndexOfPosX);
            final float _tmpPosY;
            _tmpPosY = _cursor.getFloat(_cursorIndexOfPosY);
            final int _tmpWidth;
            _tmpWidth = _cursor.getInt(_cursorIndexOfWidth);
            final int _tmpHeight;
            _tmpHeight = _cursor.getInt(_cursorIndexOfHeight);
            final String _tmpContentJson;
            if (_cursor.isNull(_cursorIndexOfContentJson)) {
              _tmpContentJson = null;
            } else {
              _tmpContentJson = _cursor.getString(_cursorIndexOfContentJson);
            }
            _item = new BlockEntity(_tmpId,_tmpProjectId,_tmpTitle,_tmpType,_tmpPosX,_tmpPosY,_tmpWidth,_tmpHeight,_tmpContentJson);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
