Sass Plugin for dotCMS
======================

Using jRuby this plugin replaces the normal asset downloading for
extensions .sass and .scss with the output of the Sass parser.
It uses an unmodified version of sass gem 3.2.7 on 1.9 jRuby runtime.
The syntax style (scss or sass) is based on your file extesion.

I couldn't do this without the guys in <a href="http://stackoverflow.com/questions/9844668/how-can-i-use-gems-in-a-jar-with-embedded-jruby">this stackoverflow thread</a>

There is some hasle
===================
Until we fix issue <a href="https://github.com/dotCMS/dotCMS/issues/2388">#2388</a> (maybe for dotCMS 2.3) this plugin
requires an aditional step to deploy properly. 
After deploy your plugins run this command from dotCMS home directory.

`cp -ra plugins/com.dotcms.sass/ROOT/dotCMS/WEB-INF/sass-gem/* dotCMS/WEB-INF/sass-gem/`


Licence
=======
GPL v2

