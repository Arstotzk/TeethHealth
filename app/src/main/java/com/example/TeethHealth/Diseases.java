package com.example.TeethHealth;

import android.graphics.Bitmap;
import android.widget.TextView;

public class Diseases{

    private final Bitmap bitmap;
    private final TextView tvStatus;

    public Diseases(Bitmap _bitmap, TextView _tvStatus)
    {
        this.bitmap = _bitmap;
        this.tvStatus = _tvStatus;
    }
    public static TextView GetTvStatus(Diseases dise)
    {
        return dise.tvStatus;
    }
    public static Bitmap GetBitmap(Diseases dise)
    {
        return dise.bitmap;
    }
}
