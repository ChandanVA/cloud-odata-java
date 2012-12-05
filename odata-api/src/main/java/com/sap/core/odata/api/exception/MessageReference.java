package com.sap.core.odata.api.exception;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A {@link MessageReference} references to the used message for a {@link ODataHttpException} and is used to support 
 * internationalization and translation of exception messages for all {@link ODataHttpException} and sub classes.
 * Theses classes all contains  an {@link MessageReference} object which can be mapped to a related key and message in the resource bundles.
 */
public abstract class MessageReference {

  protected final String key;
  protected List<Object> content = null;

  private MessageReference(String key) {
    this(key, null);
  }

  private MessageReference(String key, List<Object> content) {
    this.key = key;
    this.content = content;
  }

  /**
   * Create a {@link MessageReference} for given <code>class</code> and <code>key</code>.
   * These combination of <code>class</code> and <code>key</code> has to be provided by a resource bundle.
   * 
   * @param clazz
   *        {@link ODataHttpException} for which this {@link MessageReference} should be used.
   * @param key
   *        Unique key (in context of {@link ODataHttpException}) for reference to message in resource bundle
   * @return created {@link MessageReference}
   */
  public static MessageReference create(Class<? extends ODataException> clazz, String key) {
    return new SimpleMessageReference(clazz.getName() + "." + key);
  }

  /**
   * Returns message key.
   * 
   * @return
   */
  public String getKey() {
    return key;
  }

  /**
   * Add given content to message reference.
   * 
   * @param content
   * @return
   */
  public MessageReference addContent(Object... content) {
    return new SimpleMessageReference(this.key, content);
  }

  /**
   * Receive content for this {@link MessageReference}.
   * Beware that returned list is immutable.
   * 
   * @return
   */
  // because of use of 'Collections.EMPTY_LIST' we need a @SuppressWarnings("unchecked") annotation
  @SuppressWarnings("unchecked")
  public List<Object> getContent() {
    if (content == null)
      return Collections.unmodifiableList(Collections.EMPTY_LIST);

    return Collections.unmodifiableList(content);
  }

  /**
   * Simple inner class for realization of {@link MessageReference} interface.
   */
  private static class SimpleMessageReference extends MessageReference {
    public SimpleMessageReference(String implKey) {
      super(implKey);
    }

    public SimpleMessageReference(String implKey, List<Object> content) {
      super(implKey, content);
    }

    public SimpleMessageReference(String implKey, Object... content) {
      super(implKey, Arrays.asList(content));
    }

    @Override
    public MessageReference addContent(Object... content) {
      if (this.content == null)
        this.content = new ArrayList<Object>();
      
      for (Object object : content)
        this.content.add(object);
      return this;
    }
  }

  public MessageReference create() {
    return new SimpleMessageReference(this.key);
  }
}