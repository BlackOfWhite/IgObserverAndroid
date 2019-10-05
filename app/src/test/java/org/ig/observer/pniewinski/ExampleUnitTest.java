package org.ig.observer.pniewinski;

import static org.ig.observer.pniewinski.model.User.prettyCount;
import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

  @Test
  public void addition_isCorrect() throws Exception {
    assertEquals(4, 2 + 2);
  }

  @Test
  public void prettyNumbers() {
    Assert.assertEquals("789", prettyCount(789));
    Assert.assertEquals("5,8k", prettyCount(5821));
    Assert.assertEquals("101,8k", prettyCount(101808));
    Assert.assertEquals("7,9M", prettyCount(7898562));
    Assert.assertEquals("17,9M", prettyCount(17898562));
  }
}