require 'sass'
sass_engine = Sass::Engine.new($sasscode, {:syntax=>$ext.to_sym})
sass_engine.render

