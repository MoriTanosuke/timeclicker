package de.kopis.timeclicker.panels;

import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.panel.FeedbackPanel;

public class CustomFeedbackPanel extends FeedbackPanel {

    public CustomFeedbackPanel(String id) {
        super(id);
    }

    @Override
    protected String getCSSClass(FeedbackMessage message) {
        String css;
        switch (message.getLevel()) {
            case FeedbackMessage.SUCCESS:
                css = "alert alert-success";
                break;
            case FeedbackMessage.INFO:
                css = "alert alert-info";
                break;
            case FeedbackMessage.WARNING:
                css = "alert alert-warning";
                break;
            case FeedbackMessage.ERROR:
                css = "alert alert-error";
                break;
            default:
                css = "alert alert-info";
        }

        return css;
    }
}