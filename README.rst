===============
Service Monitor
===============

|Build Status| |codecov.io| |license-mit|

=====

Application performance monitoring.

Currently in development.

=====

Features:
    - Server cpu/ram/io monitoring
    - NGINX stats
    - API availability & uptime
    - MySQL stats
    - Docker stats
    - Email, Telegram, Webhook notifications

=====

===========
How to use:
===========

For simplicity, you can use docker-compose to start application container and MariaDB (for data storage) with default
configuration and frontend::

   wget https://raw.githubusercontent.com/nikitavbv/ServiceMonitor/master/docker-compose.yaml
   docker-compose up --build -d

The app will be listening on port 80.

To stop::

   docker-compose down

Alternatively, you can use individual container (this requires linking to database container)::

   docker run --name servicemonitor -p 80:8080 --link db:db --env DB_URL=jdbc:mysql://db:3306/servicemonitor --env DB_USERNAME=user --env DB_PASSWORD=password -d nikitavbv/servicemonitor

(Database URL and credentials are passed via environmental variables)

.. |Build Status| image:: https://img.shields.io/travis/nikitavbv/ServiceMonitor/master.svg?label=Build%20status
   :target: https://travis-ci.org/nikitavbv/ServiceMonitor
.. |codecov.io| image:: https://img.shields.io/codecov/c/github/nikitavbv/ServiceMonitor/master.svg?label=coverage
   :target: https://codecov.io/github/nikitavbv/ServiceMonitor?branch=master
.. |license-mit| image:: https://img.shields.io/badge/License-MIT-yellow.svg
   :target: https://opensource.org/licenses/MIT
