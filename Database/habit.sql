USE [habit]
GO
/****** Object:  Table [dbo].[feedback]    Script Date: 11/6/2018 7:22:17 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[feedback](
	[feedback_id] [int] IDENTITY(1,1) NOT NULL,
	[user_id] [int] NULL,
	[start_num] [int] NULL,
	[feedback_description] [text] NULL,
 CONSTRAINT [PK_feedback] PRIMARY KEY CLUSTERED 
(
	[feedback_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

GO
/****** Object:  Table [dbo].[group]    Script Date: 11/6/2018 7:22:17 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[group](
	[group_id] [int] IDENTITY(1,1) NOT NULL,
	[group_name] [nvarchar](50) NULL,
	[parent_id] [nvarchar](50) NULL,
	[group_icon] [nvarchar](50) NULL,
	[group_description] [nvarchar](50) NULL,
 CONSTRAINT [PK_group] PRIMARY KEY CLUSTERED 
(
	[group_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
/****** Object:  Table [dbo].[habit]    Script Date: 11/6/2018 7:22:17 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[habit](
	[habit_id] [int] IDENTITY(1,1) NOT NULL,
	[user_id] [int] NULL,
	[group_id] [int] NULL,
	[monitor_id] [int] NOT NULL,
	[habit_name] [text] NULL,
	[habit_type] [bit] NULL,
	[monitor_type] [bit] NULL,
	[monitor_unit] [text] NULL,
	[monitor_number] [int] NULL,
	[start_date] [date] NULL,
	[end_date] [date] NULL,
	[create_date] [date] NULL,
	[habit_color] [text] NULL,
	[habit_description] [text] NULL,
 CONSTRAINT [PK_habit_1] PRIMARY KEY CLUSTERED 
(
	[habit_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

GO
/****** Object:  Table [dbo].[monitor_date]    Script Date: 11/6/2018 7:22:17 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[monitor_date](
	[monitor_id] [int] IDENTITY(1,1) NOT NULL,
	[habit_id] [int] NULL,
	[mon] [bit] NULL,
	[tue] [bit] NULL,
	[web] [bit] NULL,
	[thu] [bit] NULL,
	[fri] [bit] NULL,
	[sat] [bit] NULL,
	[sun] [bit] NULL,
 CONSTRAINT [PK_monitor_date] PRIMARY KEY CLUSTERED 
(
	[monitor_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
/****** Object:  Table [dbo].[reminder]    Script Date: 11/6/2018 7:22:17 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[reminder](
	[reminder_id] [int] IDENTITY(1,1) NOT NULL,
	[habit_id] [int] NULL,
	[reminder_time] [text] NULL,
	[repeat_time] [int] NULL,
	[reminder_description] [text] NULL,
 CONSTRAINT [PK_reminder] PRIMARY KEY CLUSTERED 
(
	[reminder_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

GO
/****** Object:  Table [dbo].[tracking]    Script Date: 11/6/2018 7:22:17 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[tracking](
	[tracking_id] [int] IDENTITY(1,1) NOT NULL,
	[habit_id] [int] NULL,
	[current_date] [text] NULL,
	[count] [int] NULL,
	[tracking_description] [text] NULL,
 CONSTRAINT [PK_tracking] PRIMARY KEY CLUSTERED 
(
	[tracking_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

GO
/****** Object:  Table [dbo].[user]    Script Date: 11/6/2018 7:22:17 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[user](
	[user_id] [int] IDENTITY(1,1) NOT NULL,
	[username] [nvarchar](50) NULL,
	[password] [nvarchar](50) NULL,
	[phone] [text] NULL,
	[email] [nvarchar](50) NULL,
	[birthdate] [date] NULL,
	[gender] [bit] NULL,
	[user_icon] [text] NULL,
	[avatar] [text] NULL,
	[user_description] [text] NULL,
 CONSTRAINT [PK_user] PRIMARY KEY CLUSTERED 
(
	[user_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

GO
ALTER TABLE [dbo].[feedback]  WITH CHECK ADD  CONSTRAINT [FK_feedback_user] FOREIGN KEY([user_id])
REFERENCES [dbo].[user] ([user_id])
GO
ALTER TABLE [dbo].[feedback] CHECK CONSTRAINT [FK_feedback_user]
GO
ALTER TABLE [dbo].[habit]  WITH CHECK ADD  CONSTRAINT [FK_habit_group] FOREIGN KEY([group_id])
REFERENCES [dbo].[group] ([group_id])
GO
ALTER TABLE [dbo].[habit] CHECK CONSTRAINT [FK_habit_group]
GO
ALTER TABLE [dbo].[habit]  WITH CHECK ADD  CONSTRAINT [FK_habit_monitor_date1] FOREIGN KEY([monitor_id])
REFERENCES [dbo].[monitor_date] ([monitor_id])
GO
ALTER TABLE [dbo].[habit] CHECK CONSTRAINT [FK_habit_monitor_date1]
GO
ALTER TABLE [dbo].[habit]  WITH CHECK ADD  CONSTRAINT [FK_habit_user] FOREIGN KEY([user_id])
REFERENCES [dbo].[user] ([user_id])
GO
ALTER TABLE [dbo].[habit] CHECK CONSTRAINT [FK_habit_user]
GO
ALTER TABLE [dbo].[monitor_date]  WITH CHECK ADD  CONSTRAINT [FK_monitor_date_habit] FOREIGN KEY([habit_id])
REFERENCES [dbo].[habit] ([habit_id])
GO
ALTER TABLE [dbo].[monitor_date] CHECK CONSTRAINT [FK_monitor_date_habit]
GO
ALTER TABLE [dbo].[reminder]  WITH CHECK ADD  CONSTRAINT [FK_reminder_habit] FOREIGN KEY([habit_id])
REFERENCES [dbo].[habit] ([habit_id])
GO
ALTER TABLE [dbo].[reminder] CHECK CONSTRAINT [FK_reminder_habit]
GO
ALTER TABLE [dbo].[tracking]  WITH CHECK ADD  CONSTRAINT [FK_tracking_habit] FOREIGN KEY([habit_id])
REFERENCES [dbo].[habit] ([habit_id])
GO
ALTER TABLE [dbo].[tracking] CHECK CONSTRAINT [FK_tracking_habit]
GO
