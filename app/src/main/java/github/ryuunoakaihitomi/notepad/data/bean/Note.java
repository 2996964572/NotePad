package github.ryuunoakaihitomi.notepad.data.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class Note implements Parcelable {

    public static final Creator<Note> CREATOR = new Creator<Note>() {

        @Override
        public Note createFromParcel(Parcel in) {
            return new Note(in);
        }

        @Override
        public Note[] newArray(int size) {
            return new Note[size];
        }
    };
    private String title, body;
    private long id, updateTime;

    public Note() {
    }

    private Note(Parcel in) {
        title = in.readString();
        body = in.readString();
        id = in.readLong();
        updateTime = in.readLong();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(body);
        dest.writeLong(id);
        dest.writeLong(updateTime);
    }
}
