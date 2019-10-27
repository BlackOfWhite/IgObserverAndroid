package org.ig.observer.pniewinski;

import static org.ig.observer.pniewinski.model.User.prettyCount;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
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

  @Test
  public void leftJoinLists() {
    List<Book> left = Arrays.asList(new Book("1", "Merry"), new Book("2", "Christmas"),
        new Book("3", "And"), new Book("4", "Happy NY"));
    List<Book> right = Arrays.asList(new Book("1", "Merry Christmas"), new Book("3", "Happy New Year"),
        new Book("5", "Five"));
    List<Book> finalList = new ArrayList<>();
    for (Book l : left) {
      finalList.add(leftJoinItem(l, right));
    }
    System.out.println(finalList);
  }

  private <T> T leftJoinItem(T leftItem, List<T> right) {
    for (T i : right) {
      if (i.equals(leftItem)) {
        return i;
      }
    }
    return leftItem;
  }

  private class Book {

    private String id;
    private String title;

    public Book(String id, String title) {
      this.id = id;
      this.title = title;
    }

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }
    public String getTitle() {
      return title;
    }

    public void setTitle(String title) {
      this.title = title;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }

      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      Book book = (Book) o;

      return new EqualsBuilder()
          .append(getId(), book.getId())
          .isEquals();
    }

    @Override
    public int hashCode() {
      return new HashCodeBuilder(17, 37)
          .append(getId())
          .toHashCode();
    }

    @Override
    public String toString() {
      return "Book{" +
          "id='" + id + '\'' +
          ", title='" + title + '\'' +
          '}';
    }
  }
}