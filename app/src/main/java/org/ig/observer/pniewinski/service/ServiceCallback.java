package org.ig.observer.pniewinski.service;

import java.util.ArrayList;
import org.ig.observer.pniewinski.model.User;

/**
 * Used by service to communicate with MainActivity
 */
public interface ServiceCallback {

  ArrayList<User> usersChanged(ArrayList<User> list);
}
