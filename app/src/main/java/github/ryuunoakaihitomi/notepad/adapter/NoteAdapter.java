package github.ryuunoakaihitomi.notepad.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import github.ryuunoakaihitomi.notepad.R;
import github.ryuunoakaihitomi.notepad.data.bean.Note;
import github.ryuunoakaihitomi.notepad.util.TimeUtils;
import github.ryuunoakaihitomi.notepad.widget.ListTitleTextView;


public class NoteAdapter extends ArrayAdapter<Note> {

    private static final String TAG = "NoteAdapter";

    private final int mResId;
    private final List<Note> mNoteList;

    public NoteAdapter(@NonNull Context context, int resource, @NonNull List<Note> objects) {
        super(context, resource, objects);
        mResId = resource;
        mNoteList = objects;
    }

    public void reload(List<Note> noteList) {
        mNoteList.clear();
        mNoteList.addAll(noteList);
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return mNoteList.get(position).getId();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Note note = getItem(position);
        if (note == null) {
            Log.e(TAG, "getView: note == null");
            return new View(getContext());
        }
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(mResId, parent, false);
            holder = new ViewHolder();
            holder.mTitleOverview = convertView.findViewById(R.id.tv_note_title_overview);
            holder.mBodyOverview = convertView.findViewById(R.id.tv_note_body_overview);
            holder.mUpdateTimeOverview = convertView.findViewById(R.id.tv_note_update_time_overview);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.mTitleOverview.setText(note.getTitle());
        holder.mBodyOverview.setText(note.getBody());
        holder.mUpdateTimeOverview.setText(TimeUtils.getLocalFormatText(note.getUpdateTime()));
        return convertView;
    }

    private static class ViewHolder {
        ListTitleTextView mTitleOverview;
        TextView mBodyOverview, mUpdateTimeOverview;
    }
}
