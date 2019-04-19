package async;

import android.content.Context;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;

public class LoadJSON extends AsyncTask<Void, Void, String> {

    private TaskCompleted mCallback;
    private Context context;

    public LoadJSON(Context myContext) {
        this.context = myContext;
        this.mCallback = (TaskCompleted) context;
    }


    @Override
    protected String doInBackground(Void... voids) {
        String json = null;
        try {
            InputStream is = context.getAssets().open("city.list.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    @Override
    protected void onPostExecute(String s) {

        mCallback.onTaskComplete(s);
    }
}
