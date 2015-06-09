CREATE TABLE PV_User
(
  Id               BIGINT       NOT NULL,
  Username         VARCHAR(50)  NOT NULL UNIQUE,
  Password         VARCHAR(100) NOT NULL,
  Change_Password  BOOL         NOT NULL,
  Failed_Logon_Attempts INTEGER NOT NULL DEFAULT 0,
  Locked           BOOL         NOT NULL,
  Dummy            BOOL         NOT NULL,
  Email            VARCHAR(200) NOT NULL,
  Forename         VARCHAR(500) NOT NULL,
  Surname          VARCHAR(500) NOT NULL,
  Role_Description VARCHAR(50),
  Date_Of_Birth    DATE,
  Verification_Code    VARCHAR(200),
  Email_Verified   BOOL         NOT NULL DEFAULT FALSE,
  Contact_Number   VARCHAR(50),
  Last_Login       TIMESTAMP,
  Last_Login_Ip_Address    VARCHAR(50),
  Deleted          BOOLEAN      NOT NULL DEFAULT FALSE,
  Picture          TEXT,
  Start_Date       DATE         NOT NULL,
  End_Date         DATE,
  Creation_Date    TIMESTAMP    NOT NULL,
  Created_By       BIGINT       NOT NULL REFERENCES PV_User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By  BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_User_Migration
(
  Id                      BIGINT       NOT NULL,
  PatientView1_User_Id    BIGINT       NOT NULL,
  PatientView2_User_Id    BIGINT,
  Observation_Count       BIGINT,
  Status                  VARCHAR(50)  NOT NULL,
  Information             TEXT,
  Creation_Date           TIMESTAMP    NOT NULL,
  Created_By              BIGINT       NOT NULL REFERENCES PV_User (Id),
  Last_Update_Date        TIMESTAMP,
  Last_Updated_By         BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Lookup_Type (
  Id               BIGINT    NOT NULL,
  Lookup_Type      VARCHAR(50) UNIQUE,
  Description      TEXT,
  Creation_Date    TIMESTAMP NOT NULL,
  Created_By       BIGINT REFERENCES PV_User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By  BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Lookup_Value (
  Id               BIGINT       NOT NULL,
  Lookup_Type_Id   BIGINT       NOT NULL REFERENCES PV_Lookup_Type (Id),
  Value            VARCHAR(100) NOT NULL,
  Description      TEXT,
  Creation_Date    TIMESTAMP    NOT NULL,
  Created_By       BIGINT REFERENCES PV_User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By  BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Group
(
  Id               BIGINT    NOT NULL,
  Group_Name       VARCHAR(200) UNIQUE,
  Group_Short_Name VARCHAR(200),
  Code             VARCHAR(50),
  Sftp_User        VARCHAR(255),
  Type_Id          BIGINT REFERENCES PV_Lookup_Value (Id) NOT NULL,
  Parent_Group_Id  BIGINT,
  Fhir_Resource_Id UUID,
  Visible          BOOLEAN,
  Visible_To_Join  BOOLEAN,
  Address_1        TEXT,
  Address_2        TEXT,
  Address_3        TEXT,
  Postcode         VARCHAR(255),
  Last_Import_Date TIMESTAMP,
  Creation_Date    TIMESTAMP NOT NULL,
  Created_By       BIGINT REFERENCES PV_User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By  BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Contact_Point
(
  Id               BIGINT    NOT NULL,
  User_Id          BIGINT    REFERENCES PV_User (Id),
  Group_Id         BIGINT    REFERENCES PV_Group (Id),
  Type_Id          BIGINT    REFERENCES PV_Lookup_Value (Id) NOT NULL,
  Content          TEXT      NOT NULL,
  Creation_Date    TIMESTAMP NOT NULL,
  Created_By       BIGINT REFERENCES PV_User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By  BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Group_Relationship
(
  Id               BIGINT    NOT NULL,
  Source_Group_Id         BIGINT    NOT NULL,
  Object_Group_Id  BIGINT    REFERENCES PV_Group (Id) NOT NULL,
  Relationship_Type VARCHAR(100) NOT NULL,
  Start_Date       DATE         NOT NULL,
  End_Date         DATE,
  Creation_Date    TIMESTAMP NOT NULL,
  Created_By       BIGINT REFERENCES PV_User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By  BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Role
(
  Id               BIGINT      NOT NULL,
  Role_Name        VARCHAR(50) NOT NULL UNIQUE,
  Type_Id          BIGINT REFERENCES PV_Lookup_Value (Id) NOT NULL,
  Level            INTEGER   NOT NULL,
  Visible          BOOLEAN,
  Description      VARCHAR(255),
  Creation_Date    TIMESTAMP   NOT NULL,
  Created_By       BIGINT      NOT NULL REFERENCES PV_User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By  BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);


CREATE TABLE PV_User_Group_Role
(
  Id               BIGINT    NOT NULL,
  User_Id          BIGINT    NOT NULL REFERENCES PV_User (Id),
  Group_Id         BIGINT REFERENCES PV_Group (Id),
  Role_Id          BIGINT    NOT NULL REFERENCES PV_Role (Id),
  Start_Date       DATE,
  End_Date         DATE,
  Creation_Date    TIMESTAMP NOT NULL,
  Created_By       BIGINT REFERENCES PV_User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By  BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Feature
(
  Id                BIGINT    NOT NULL,
  Feature_Name      VARCHAR(50) NOT NULL UNIQUE,
  Description       VARCHAR(100),
  Parent_Feature_Id BIGINT,
  Start_Date        DATE,
  End_Date          DATE,
  Creation_Date     TIMESTAMP NOT NULL,
  Created_By        BIGINT REFERENCES PV_User (Id),
  Last_Update_Date  TIMESTAMP,
  Last_Updated_By   BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Feature_Feature_Type
(
  Id                BIGINT NOT NULL,
  Feature_Id        BIGINT REFERENCES PV_Feature (Id) NOT NULL,
  Type_Id           BIGINT REFERENCES PV_Lookup_Value (Id) NOT NULL,
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Feature_User
(
  Id               BIGINT    NOT NULL,
  User_Id          BIGINT    NOT NULL REFERENCES PV_User (Id),
  Feature_Id       BIGINT    NOT NULL REFERENCES PV_Feature (Id),
  Opt_In_Status    BOOL,
  Opt_In_Hidden    BOOL,
  Opt_Out_Hidden   BOOL,
  Opt_In_Date      TIMESTAMP,
  Start_Date       DATE,
  End_Date         DATE,
  Creation_Date    TIMESTAMP NOT NULL,
  Created_By       BIGINT REFERENCES PV_User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By  BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Feature_Group
(
  Id               BIGINT    NOT NULL,
  Group_Id         BIGINT    NOT NULL REFERENCES PV_Group (Id),
  Feature_Id       BIGINT    NOT NULL REFERENCES PV_Feature (Id),
  Start_Date       DATE      NOT NULL,
  End_Date         DATE,
  Creation_Date    TIMESTAMP NOT NULL,
  Created_By       BIGINT REFERENCES PV_User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By  BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);


CREATE TABLE PV_User_Token
(
  Id              BIGINT      NOT NULL,
  User_Id         BIGINT      NOT NULL REFERENCES PV_User (Id),
  Token           VARCHAR(50) NOT NULL UNIQUE,
  Parent_Token_Id BIGINT      REFERENCES PV_User_Token (Id),
  Creation_Date   TIMESTAMP   NOT NULL,
  Expiration_Date TIMESTAMP,
  PRIMARY KEY (Id)
);

CREATE TABLE PV_News_Item (
  Id               BIGINT    NOT NULL,
  Heading          VARCHAR(100),
  Story            TEXT      NOT NULL,
  Creation_Date    TIMESTAMP NOT NULL,
  Created_By       BIGINT REFERENCES PV_User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By  BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_News_Link (
  Id            BIGINT    NOT NULL,
  News_Id       BIGINT    NOT NULL REFERENCES PV_News_Item (Id),
  Group_Id      BIGINT REFERENCES PV_Group (Id),
  Role_Id       BIGINT REFERENCES PV_Role (Id),
  Creation_Date TIMESTAMP NOT NULL,
  Created_By    BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Conversation (
  Id               BIGINT       NOT NULL,
  Type             VARCHAR(255) NOT NULL,
  Image_Data       TEXT,
  Rating           INTEGER,
  Status           INTEGER,
  Open             BOOL         NOT NULL,
  Title            VARCHAR(200) NOT NULL,
  Creation_Date    TIMESTAMP    NOT NULL,
  Created_By       BIGINT REFERENCES PV_User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By  BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (id)
);

CREATE TABLE PV_Message (
  Id              BIGINT    NOT NULL,
  Conversation_Id BIGINT    NOT NULL  REFERENCES PV_Conversation (Id),
  Type            VARCHAR(255) NOT NULL,
  Message         TEXT      NOT NULL,
  User_Id         BIGINT    REFERENCES PV_User (Id),
  Creation_Date   TIMESTAMP NOT NULL,
  Created_By      BIGINT REFERENCES PV_User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By  BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Conversation_User (
  Id              BIGINT    NOT NULL,
  Conversation_Id BIGINT    NOT NULL REFERENCES PV_Conversation (Id),
  User_Id         BIGINT    NOT NULL REFERENCES PV_User (Id),
  Anonymous       BOOL      NOT NULL,
  Creation_Date   TIMESTAMP NOT NULL,
  Created_By      BIGINT REFERENCES PV_User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By  BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Conversation_User_Label (
  Id                      BIGINT    NOT NULL,
  Conversation_User_Id    BIGINT    NOT NULL REFERENCES PV_Conversation_User (Id),
  Conversation_Label      VARCHAR(255) NOT NULL,
  Creation_Date           TIMESTAMP NOT NULL,
  Created_By              BIGINT REFERENCES PV_User (Id),
  Last_Update_Date        TIMESTAMP,
  Last_Updated_By         BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Message_Read_Receipt (
  Id            BIGINT    NOT NULL,
  Message_Id    BIGINT    NOT NULL  REFERENCES PV_Message (Id),
  User_Id       BIGINT    NOT NULL REFERENCES PV_User (Id),
  Creation_Date TIMESTAMP NOT NULL,
  PRIMARY KEY (Id)
);

CREATE TABLE PV_User_Information (
  Id            BIGINT    NOT NULL,
  User_Id       BIGINT    NOT NULL REFERENCES PV_User (Id),
  Type          VARCHAR(255)  NOT NULL,
  Value         TEXT      NOT NULL,
  Creation_Date   TIMESTAMP NOT NULL,
  Created_By      BIGINT REFERENCES PV_User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By  BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Code (
  Id               BIGINT    NOT NULL,
  Code             VARCHAR(100),
  Type_Id          BIGINT    NOT NULL REFERENCES PV_Lookup_Value (Id),
  Display_Order    INTEGER   ,
  Description      VARCHAR(255),
  Standard_Type_Id BIGINT    NOT NULL REFERENCES PV_Lookup_Value (Id),
  Creation_Date    TIMESTAMP NOT NULL,
  Created_By       BIGINT REFERENCES PV_User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By  BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Location (
  Id            BIGINT    NOT NULL,
  Group_Id      BIGINT    NOT NULL  REFERENCES PV_Group (Id),
  Label         TEXT      NOT NULL,
  Name          TEXT      NOT NULL,
  Phone         TEXT,
  Address       TEXT,
  Web           TEXT,
  Email         TEXT,
  Creation_Date    TIMESTAMP NOT NULL,
  Created_By       BIGINT REFERENCES PV_User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By  BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Log (
  Id            BIGINT    NOT NULL,
  User_Id       BIGINT REFERENCES PV_User (Id),
  Source        VARCHAR(50),
  Message       VARCHAR(500),
  Creation_Date TIMESTAMP NOT NULL,
  Created_By    BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Audit (
  Id            BIGINT    NOT NULL,
  Action        VARCHAR(200),
  Source_Object_Type    VARCHAR(50),
  Source_Object_Id      BIGINT,
  Pre_Value     VARCHAR(500),
  Post_Value    VARCHAR(500),
  Actor_Id      BIGINT   REFERENCES PV_User (Id),
  Identifier    VARCHAR(50),
  Group_Id      BIGINT   REFERENCES PV_Group (Id),
  Information   TEXT,
  Xml           TEXT,
  Username      TEXT,
  Creation_Date TIMESTAMP NOT NULL,
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Observation_Heading (
  Id                    BIGINT    NOT NULL,
  Code                  VARCHAR(200) NOT NULL,
  Heading               TEXT,
  Name                  TEXT,
  Normal_Range          TEXT,
  Units                 TEXT,
  Min_Graph             NUMERIC(19, 2),
  Max_Graph             NUMERIC(19, 2),
  Info_Link             TEXT,
  Default_Panel         BIGINT,
  Default_Panel_Order   BIGINT,
  Decimal_Places        BIGINT,
  Creation_Date         TIMESTAMP NOT NULL,
  Created_By            BIGINT REFERENCES PV_User (Id),
  Last_Update_Date      TIMESTAMP,
  Last_Updated_By       BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Observation_Heading_Group (
  Id                    BIGINT    NOT NULL,
  Observation_Heading_Id  BIGINT    NOT NULL,
  Group_Id              BIGINT    NOT NULL,
  Panel                 BIGINT,
  PanelOrder            BIGINT,
  Creation_Date         TIMESTAMP NOT NULL,
  Created_By            BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Result_Cluster
(
  Id      BIGINT NOT NULL,
  Name    TEXT NOT NULL,
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Result_Cluster_Observation_Heading
(
  Id                        BIGINT NOT NULL,
  Result_Cluster_Id         BIGINT REFERENCES PV_Result_Cluster (Id) NOT NULL,
  Observation_Heading_Id    BIGINT REFERENCES PV_Observation_Heading (Id) NOT NULL,
  Display_Order             BIGINT NOT NULL,
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Module (
  Id               BIGINT    NOT NULL,
  Name             VARCHAR(200),
  Description      VARCHAR(200),
  Route            VARCHAR(2048),
  Creation_Date    TIMESTAMP NOT NULL,
  Created_By       BIGINT REFERENCES PV_User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By  BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);


CREATE TABLE PV_Module_Group (
  Id               BIGINT    NOT NULL,
  Module_Id        BIGINT REFERENCES PV_Module (Id),
  Group_Id         BIGINT REFERENCES PV_Group (Id),
  Start_Date       DATE      NOT NULL,
  End_Date         DATE,
  Creation_Date    TIMESTAMP NOT NULL,
  Created_By       BIGINT REFERENCES PV_User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By  BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Module_Role (
  Id               BIGINT    NOT NULL,
  Module_Id        BIGINT REFERENCES PV_Module (Id),
  Role_Id          BIGINT    NOT NULL REFERENCES PV_Role (Id),
  Start_Date       DATE      NOT NULL,
  End_Date         DATE,
  Creation_Date    TIMESTAMP NOT NULL,
  Created_By       BIGINT REFERENCES PV_User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By  BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Link (
  Id               BIGINT    NOT NULL,
  Type_Id          BIGINT REFERENCES PV_Lookup_Value (Id),
  Code_Id          BIGINT REFERENCES PV_Code (Id),
  Group_Id         BIGINT REFERENCES PV_Group (Id),
  Link             VARCHAR(2048),
  Name             VARCHAR(200),
  Display_Order    INTEGER       NOT NULL,
  Creation_Date    TIMESTAMP NOT NULL,
  Created_By       BIGINT REFERENCES PV_User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By  BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Shared_Thought (
  Id                   BIGINT    NOT NULL,
  Conversation_Id      BIGINT    REFERENCES PV_Conversation (Id),
  Positive             BOOL      NOT NULL,
  Anonymous            BOOL      NOT NULL,
  Submitted            BOOL      NOT NULL,
  Patient              BOOL      NOT NULL,
  Principal_Carer      BOOL      NOT NULL,
  Relative             BOOL      NOT NULL,
  Friend               BOOL      NOT NULL,
  Other                BOOL      NOT NULL,
  Other_Specify        VARCHAR(255),
  About_Me             BOOL      NOT NULL,
  About_Other          BOOL      NOT NULL,
  Ongoing              BOOL      NOT NULL,
  Location             VARCHAR(2048),
  When_Occurred        VARCHAR(2048),
  Description          VARCHAR(2048),
  Suggested_Action     VARCHAR(2048),
  Concern_Reason       VARCHAR(2048),
  Recurrence           INTEGER,
  Recurrence_Specify   VARCHAR(2048),
  Serious              INTEGER,
  Submit_Date          TIMESTAMP,
  Creation_Date        TIMESTAMP NOT NULL,
  Created_By           BIGINT    REFERENCES PV_User (Id),
  Last_Update_Date     TIMESTAMP,
  Last_Updated_By      BIGINT    REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Route (
  Id                   BIGINT        NOT NULL,
  Type_Id              BIGINT        NOT NULL  REFERENCES PV_Lookup_Value (Id),
  Display_Order        INTEGER       NOT NULL,
  Url                  VARCHAR(2048) NOT NULL,
  Controller           VARCHAR(255)  NOT NULL,
  Template_Url         VARCHAR(2048) NOT NULL,
  Title                VARCHAR(255)  NOT NULL,
  Creation_Date        TIMESTAMP     NOT NULL,
  Created_By           BIGINT        REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Route_Link (
  Id                   BIGINT        NOT NULL,
  Route_Id             BIGINT        NOT NULL  REFERENCES PV_Route (Id),
  Group_Id             BIGINT        REFERENCES PV_Group (Id),
  Role_Id              BIGINT        REFERENCES PV_Role (Id),
  Feature_Id           BIGINT        REFERENCES PV_Feature (Id),
  Creation_Date        TIMESTAMP     NOT NULL,
  Created_By           BIGINT        REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Identifier
(
  Id               BIGINT      NOT NULL,
  User_Id          BIGINT REFERENCES PV_User (Id) NOT NULL,
  Type_Id          BIGINT REFERENCES PV_Lookup_Value (Id) NOT NULL,
  Identifier       VARCHAR(200)   NOT NULL,
  Start_Date       DATE,
  End_Date         DATE,
  Creation_Date    TIMESTAMP   NOT NULL,
  Created_By       BIGINT      NOT NULL REFERENCES PV_User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By  BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Request
(
  Id               BIGINT      NOT NULL,
  Forename         VARCHAR(500)   NOT NULL,
  Surname          VARCHAR(500) NOT NULL,
  Date_Of_Birth    DATE NOT NULL,
  Email            VARCHAR(500),
  Nhs_Number       VARCHAR(10),
  Notes            TEXT,
  Completion_Date  TIMESTAMP,
  Completed_By     BIGINT REFERENCES PV_User (Id),
  Status           VARCHAR(50),
  Type             VARCHAR(50),
  Group_Id         BIGINT REFERENCES PV_Group (Id),
  Creation_Date    TIMESTAMP NOT NULL,
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Group_Statistic (
  Id               BIGINT NOT NULL,
  Group_Id         BIGINT        REFERENCES PV_Group (Id),
  Start_Date       DATE NOT NULL,
  End_Date         DATE NOT NULL,
  Collated_Period  VARCHAR(50),
  Type_Id          BIGINT REFERENCES PV_Lookup_Value (Id) NOT NULL,
  Value            NUMERIC(19, 2) DEFAULT 0,
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Fhir_Link
(
  Id                 BIGINT       NOT NULL,
  User_Id            BIGINT       REFERENCES PV_User (Id),
  Identifier_Id      BIGINT       REFERENCES PV_Identifier (Id),
  Group_Id           BIGINT       REFERENCES PV_Group (Id),
  Resource_Id   UUID,
  Version_Id    UUID,
  Resource_Type VARCHAR(100),
  Active        BOOLEAN DEFAULT TRUE,
  Creation_Date      TIMESTAMP    NOT NULL,
  Last_Update_Date      TIMESTAMP    NOT NULL,
  PRIMARY KEY (Id)
);

CREATE TABLE PV_User_Observation_Heading
(
  Id                          BIGINT       NOT NULL,
  User_Id                     BIGINT       REFERENCES PV_User (Id) NOT NULL,
  Observation_Heading_Id      BIGINT       REFERENCES PV_Observation_Heading (Id) NOT NULL,
  Created_By                  BIGINT       REFERENCES PV_User (Id) NOT NULL,
  Creation_Date               TIMESTAMP    NOT NULL,
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Alert
(
  Id                          BIGINT       NOT NULL,
  Alert_Type                  TEXT,
  Web_Alert                   BOOLEAN      NOT NULL,
  Web_Alert_Viewed            BOOLEAN      NOT NULL,
  Email_Alert                 BOOLEAN      NOT NULL,
  Email_Alert_Sent            BOOLEAN      NOT NULL,
  User_Id                     BIGINT       REFERENCES PV_User (Id) NOT NULL,
  Observation_Heading_Id      BIGINT       REFERENCES PV_Observation_Heading (Id),
  Latest_Value                TEXT,
  Latest_Date                 TIMESTAMP,
  Created_By                  BIGINT       REFERENCES PV_User (Id) NOT NULL,
  Creation_Date               TIMESTAMP    NOT NULL,
  Last_Update_Date            TIMESTAMP,
  Last_Updated_By             BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_External_Service_Task_Queue_Item
(
  Id                          BIGINT NOT NULL,
  Url                         TEXT NOT NULL,
  Method                      TEXT NOT NULL,
  Content                     TEXT NOT NULL,
  Status                      TEXT NOT NULL,
  Response_Code               INT,
  Response_Reason             TEXT,
  Created_By                  BIGINT       REFERENCES PV_User (Id) NOT NULL,
  Creation_Date               TIMESTAMP    NOT NULL,
  Last_Update_Date            TIMESTAMP,
  Last_Updated_By             BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
)

CREATE TABLE PV_File_Data
(
  Id              BIGINT NOT NULL,
  Name            TEXT NOT NULL,
  Type            TEXT NOT NULL,
  Size            BIGINT NOT NULL,
  Content         BYTEA NOT NULL,
  Creation_Date   TIMESTAMP NOT NULL,
  PRIMARY KEY (Id)
)

CREATE TABLE PV_Symptom_Score
(
  Id              BIGINT NOT NULL,
  User_Id         BIGINT NOT NULL,
  Score           NUMERIC(10, 4) NOT NULL,
  Date            TIMESTAMP NOT NULL,
  PRIMARY KEY (Id)
)

CREATE TABLE PV_Survey
(
  Id              BIGINT NOT NULL,
  Type            TEXT NOT NULL,
  Description     TEXT,
  PRIMARY KEY (Id)
)

CREATE TABLE PV_Question_Group
(
  Id              BIGINT NOT NULL,
  Survey_Id       BIGINT NOT NULL REFERENCES PV_Survey (Id),
  Text            TEXT NOT NULL,
  Description     TEXT,
  Number          TEXT,
  Display_Order   INT,
  PRIMARY KEY (Id)
)

CREATE TABLE PV_Question
(
  Id              BIGINT NOT NULL,
  Question_Group_Id     BIGINT NOT NULL REFERENCES PV_Question_Group (Id),
  Type            TEXT NOT NULL,
  Html_Type       TEXT NOT NULL,
  Text            TEXT NOT NULL,
  Description     TEXT,
  Number          TEXT,
  Display_Order   INT,
  PRIMARY KEY (Id)
)

CREATE TABLE PV_Question_Option
(
  Id              BIGINT NOT NULL,
  Question_Id     BIGINT NOT NULL REFERENCES PV_Question (Id),
  Text            TEXT NOT NULL,
  Description     TEXT,
  PRIMARY KEY (Id)
)

CREATE SEQUENCE hibernate_sequence
INCREMENT 1
MINVALUE 1
MAXVALUE 9223372036854775807
START 1
CACHE 1;
ALTER TABLE hibernate_sequence
OWNER TO patientview;
