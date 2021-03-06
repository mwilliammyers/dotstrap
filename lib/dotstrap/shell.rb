require 'logger'

module Dotstrap
  class Dotstrap::Shell
    # TODO: cleanup shell class & split it into 3+ classes with inheritence
    attr_accessor :repo_path

    # TODO: how to handle load order? ie. load path.fish first
    def initialize(repo_path)
      @repo_path = repo_path
    end

    def configure(repo_dir = @repo_path)
      configure_fish(repo_dir) if should_configure_fish?
      configure_zsh(repo_dir)
      configure_bash(repo_dir)
      $LOG.unknown { "configuration complete" }
    end

    def unconfigure(repo_dir = @repo_path)
      return unless Dir.exist?(repo_dir)
      unconfigure_fish(repo_dir) if should_configure_fish?
      unconfigure_zsh(repo_dir)
      unconfigure_bash(repo_dir)
      FileUtils.rm_r(repo_dir, force: true, secure: true)
      $LOG.unknown { "removed: #{repo_dir}\n" }
    end

    def configure_fish(repo_dir)
      fish_functions(repo_dir).each do |f|
        link_config_file(f, File.join(Dotstrap.shell_config_home('fish'),
                                      'functions'))
      end

      fish_completions(repo_dir).each do |f|
        link_config_file(f, File.join(Dotstrap.shell_config_home('fish'),
                                   'completions'))
      end

      write_config_file(fish_configs(repo_dir), config_file('fish'))
    end

    def unconfigure_fish(repo_dir)
      fish_functions(repo_dir).each do |f|
        file = File.join(Dotstrap.shell_config_home('fish'), 'functions',
                         File.basename(f))
        rm_config_file(file)
      end

      fish_completions(repo_dir).each do |f|
        file = File.join(Dotstrap.shell_config_home('fish'), 'completions',
                         File.basename(f))
        rm_config_file(file)
      end

      unwrite_config_file(fish_configs(repo_dir), config_file('fish'))
    end

    def configure_zsh(repo_dir)
      write_config_file(zsh_configs(repo_dir), config_file('zsh'))
    end

    def unconfigure_zsh(repo_dir)
      unwrite_config_file(zsh_configs(repo_dir), config_file('zsh'))
    end

    def configure_bash(repo_dir)
      write_config_file(bash_configs(repo_dir), config_file('bash'))
    end

    def unconfigure_bash(repo_dir)
      unwrite_config_file(bash_configs(repo_dir), config_file('bash'))
    end

    def rm_config_file(file)
      FileUtils.rm_rf file
    end

    def config_file(sh, dir = Dotstrap.config_home)
      File.join(dir, "config.#{sh}")
    end

    private

    def write_config_file(repo_config_files, dotstrap_config_file)
      return if repo_config_files.empty?
      repo_config_files.each do |file|
        append_config_file(file, dotstrap_config_file)
        $LOG.info { "configured: #{file}" }
      end
    end

    def unwrite_config_file(repo_config_files, dotstrap_config_file)
      return if repo_config_files.empty?
      repo_config_files.each do |file|
        unappend_config_file(file, dotstrap_config_file)
        $LOG.info { "removed: #{file}" }
      end
    end

    # TODO: only open config file once, write all source strings at once
    def append_config_file(file_to_source, config_file)
      source_str = "source \"#{file_to_source}\"\n"
      if File.exist?(config_file)
        # Do not write source_str if we have already written it before
        return if File.readlines(config_file).grep(source_str).any?
      end
      File.open(config_file, 'a') { |f| f.write source_str }
    end

    def unappend_config_file(file_to_unsource, config_file)
      return unless File.exist?(config_file)
      Dir.mktmpdir do |tmp_dir|
        source_str = "source \"#{file_to_unsource}\""
        tmp_config_file = File.join(tmp_dir, File.basename(config_file))
        File.open(tmp_config_file, 'w') do |tmp_file|
          File.foreach(file_to_unsource) do |line|
            tmp_file.write line unless line.chomp == source_str
          end
        end
        FileUtils.mv(tmp_config_file, config_file)
      end
    end

    def link_config_file(src, dest_dir)
      return unless File.exist?(src)
      $LOG.debug { "LINK_CONFIG_FILE:#{src} to #{dest_dir}" }
      dst = File.join(dest_dir, File.basename(src))
      # FIXME: fails if dest_dir for link is a broken symbolic link
      # parent = Pathname.new(dest_dir).parent
      # FileUtils.mkdir parent unless Dir.exist? parent
      FileUtils.mkdir_p dest_dir unless Dir.exist? dest_dir
      FileUtils.ln_s src, dst, force: true
      $LOG.info { "linked: #{src} to #{dst}" }
    end

    def fish_configs(dir, excl = 'fish_prompt.fish')
      c = []
      Dir.glob(File.join(dir, '*.fish')) { |f| c << f unless f.end_with?(excl) }
      $LOG.debug { "FISH_CONFIGS:#{c}" }
      c
    end

    def fish_functions(dir, incl = ['fish_prompt.fish'])
      funcs = []
      funcs << Dir.glob(File.join(dir, 'functions', '*.fish'))
      incl.each do |f|
        f = File.join(dir, f)
        funcs << f if File.exist?(f)
      end
      $LOG.debug { "FISH_FUNCTIONS:#{funcs.flatten}" }
      funcs.flatten
    end

    def fish_completions(dir)
      c = Dir.glob(File.join(dir, 'completions', '*.fish'))
      $LOG.debug { "FISH_COMPLETIONS:#{c}" }
      c
    end

    def bash_configs(dir)
      c = Dir.glob(File.join(dir, '**', '*.{sh,bash}'))
      $LOG.debug { "BASH_CONFIGS:#{c}" }
      c
    end

    def zsh_configs(dir)
      c = Dir.glob(File.join(dir, '**', '*.{sh,zsh}'))
      $LOG.debug { "ZSH_CONFIGS:#{c}" }
      c
    end

    def should_configure_fish?
       Dir.exist?(Dotstrap.shell_config_home('fish'))
    end
  end
end
