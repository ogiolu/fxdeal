package com.progresssoft.fxdealimport.vo.payload;

import java.util.List;

public interface DataResponse<M> {

    boolean isValid();

    void setValid(boolean valid);

    List<M> getData();

    void setData(List<M> data);

    List<Message> getMessages();

    void setMessages(List<Message> messages);
}