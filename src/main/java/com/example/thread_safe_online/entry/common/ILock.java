package com.example.thread_safe_online.entry.common;

public interface ILock {
    public boolean tryLock(int id);

    public void unLock(int id);
}
