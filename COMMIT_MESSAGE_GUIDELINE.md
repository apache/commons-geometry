<!---
 Licensed to the Apache Software Foundation (ASF) under one or more
 contributor license agreements.  See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 The ASF licenses this file to You under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
-->
Commit Message Guideline
======================

You changed something and you wanna make a commit?

Thanks, but before you do so you'd better read the commit message guideline,
because we think it better to have a uniformed format of commit message,
thus we created this guideline for you.

Of course as commons is not a that united repo,
every sub-repo's main maintainer can change this file in his repo,
thus you must make sure you have read the exact file in that sub-repo if there exist one.

Commit Message components
----------------------
`COMMIT_MESSAGE ::= COMMIT_MESSAGE_HEAD (COMMIT_MESSAGE_BODY)?`

A commit message should and only should contain two components.
The first component is named Commit-Message-Head.
The second component is named Commit-Message-Body.
The Commit-Message-Head MUST be the first line of the commit message, and the Commit-Message-Body must be the other words in the commit message.

Commit Message Head
------------------
`COMMIT_MESSAGE_HEAD ::= ("[" JIRA_TICKET_NAME "]")? COMMIT_MESSAGE_HEAD_NAME`

If this commit is NOT a minor-commit, then it must have a jira-ticket.
We define the minor-commit as commit following:

1. typo-fix commit which changes only local variable names, comments, string in javadoc.
If a typo-fix commit changes a function name / field name, it is not a minor-commit.

2. comment/javadoc fix/refine which can still pass the ci on all platforms.

3. small codes refine for readability. For example:

    example1:
    
    before:

    ```java
    class Example1{
        public int demo1(){
            int a = 3 / 6 + 2;
            return a;
        }
    }
    ```
    
    after:
    
    ```java
    class Example1{
        public int demo1(){
            return 3 / 6 + 2;
        }
    }
    ```
    
    example2:
    
    before:
    ```java
    class Example2{
        public void demo2(){
            boolean f = getBoolean();
            if (f == false) {
               throw new Error(); 
            }
        }
    }
    ```
    after:
    ```java
    class Example2{
        public void demo2(){
            boolean f = getBoolean();
            if (!f) {
               throw new Error(); 
            }
        }
    }
    ```

4. other small codes refine who is very clear.

    example1:
    
    before:
    
    ```java
    import java.util.ArrayList;
    class Example1{
        public ArrayList demo1(ArrayList arrayList){
            ArrayList res = new ArrayList();
            for (Object object : arrayList) {
                res.add(object);
            }
            return res;
        }
    }
    ```
    
    after:
    
    ```java
    import java.util.ArrayList;
    class Example1{
        public ArrayList demo1(ArrayList arrayList){
            return new ArrayList(arrayList);
        }
    }
    ```

As we said before, If this commit is NOT a minor-commit, then it must have a jira ticket.

Jira Ticket Name
------------------
For creating jira ticket, please go https://issues.apache.org/jira/ .

A jira-ticket-name is something like `LANG-1546`.

Commit Message Head Name
------------------
Commit-message-head-name should be a short string who describe what you did in this pr.
It MUST be single line, and SHOULD be as short as possible.
It MUST make people have a basic idea for why we take this pr.

Commit Message Body
------------------
`COMMIT_MESSAGE_BODY ::= (COMMIT_MESSAGE_BODY_HISTORY)? (COMMIT_MESSAGE_BODY_REASON)? COMMIT_MESSAGE_BODY_CHANGES (COMMIT_MESSAGE_BODY_REMARK)?`

Commit Message Body History
------------------
Commit-message-body-history is the history of this commit.

For example, a JIRA ticket for an older related issue.

This part SHOULD NOT be here.

You SHOULD put this part to the github pull-request page, and link the github pull-request page in the JIRA ticket page.

However, if you really think it is better to give a copy here, you can do it.

But you MUST make it short and brief.

Commit Message Body Reason
------------------
Commit-message-body-reason is the reason why you make this commit.

For example, how you found this bug, or what will the fix/refine/new feature bring to the repo.

This part SHOULD NOT be here.

You SHOULD put this part to the github pull-request page, and link the github pull-request page in the JIRA ticket page.

However, if you really think it is better to give a copy here, you can do it.

But you MUST make it short and brief.

Commit Message Body Changes
------------------
Commit-message-body-changes is what changes did you make in this commit.

For example, what exact logic you changed, how it works before, and how it works after.

This is the key part of commit-message-body-history.

It is allowed to leave this part empty, but you MUST make sure, if this part is empty, then the whole commit-message-body MUST be empty.

In other words, If you have non-empty commit-message-body, you must have non-empty commit-message-body-changes.

Commit Message Body Remark
------------------
Commit-message-body-remark is the part where you put other words you think necessary in commit-message.

No example here, just, anything else you think necessary.

This part SHOULD NOT be here.

You SHOULD put this part to the github pull-request page, and link the github pull-request page in the JIRA ticket page.

However, if you really think it is better to give a copy here, you can do it.

But you MUST make it short and brief.