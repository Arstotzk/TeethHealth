package com.example.TeethHealth;

import android.view.View;

public class VisualElements {

    public static void SetVisibles(int visibles, View[] _visualElements) {
        for (int i = 0; i < _visualElements.length; i++){
            _visualElements[i].setVisibility(visibles);
        }
    }
}
