package ru.ms.actors;

import java.io.IOException;

/**
 * Created by sergey on 23.10.16.
 */
public interface IActor {
    void _do(Object settings) throws Exception;
}
