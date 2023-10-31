package com.hokuapps.startvideocall.delegate;

public interface DataListener<T> {
    void onSuccess(T obj);

    void onError(T obj);
}
