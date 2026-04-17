package ee.aleksale.releaseapp.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class AppConstants {

  public static final String STAGE_TITLE = "Release Management App";
  public static final int STAGE_WIDTH = 1100;
  public static final int STAGE_HEIGHT = 700;

  public static final int DATE_PICKER_WIDTH = 150;

  public static final int FORM_WIDTH = 320;
  public static final String PROJECT_CHOOSE_DIALOG_TITLE = "GitLab Projects";
  public static final String PROJECT_CHOOSE_HEADER_TEXT = "Select a project to add";
  public static final int PROJECT_CHOOSE_DIALOG_HEIGHT = 300;
  public static final int PROJECT_CHOOSE_DIALOG_WIDTH = 500;

  public static final String REFRESH_ICON = "↻";
  public static final String REFRESH_TOOLTIP = "Refresh tags from GitLab";

  public static final String PIPELINE_STATUS_SUCCESS = "SUCCESS";
  public static final String PIPELINE_STATUS_FAILED = "FAILED";
  public static final String PIPELINE_STATUS_RUNNING = "RUNNING";
}
