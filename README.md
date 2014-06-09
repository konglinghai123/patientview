Database Configuration
======================

Once PostgreSQL is installed enter the following commands :-

  createuser superadmin --pwprompt
  createuser patientview --pwprompt
  createdb patientview

  <code>psql patientview -c "alter role superadmin with superuser"</code><br>
  <code>psql patientview -c "alter role superadmin with createdb"</code><br>
  <code>psql patientview -c "alter role superadmin with createuser"</code><br>
<br>
  <code>psql patientview -c "alter role patientview with nosuperuser"</code><br>
  <code>psql patientview -c "alter role patientview with nocreatedb"</code><br>
  <code>psql patientview -c "alter role patientview with nocreateuser"</code><br>


Then to connect to the patientview :-

  <code>psql patientview</code>

\q to quit

Git Subtree for FHIRBase
========================

Add fhirbase as a remote then

<code>git read-tree --prefix=root/database/src/main/resources/db/fhirbase -u fhirbase/master</code><br>

<code>git pull -s subtree fhirbase master</code><br>


