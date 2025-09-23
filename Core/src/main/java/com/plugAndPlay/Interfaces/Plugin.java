package com.plugAndPlay.Interfaces;

import com.plugAndPlay.Shared.AppContext;

public interface Plugin {
    String getName();
    void execute(AppContext context);
}
