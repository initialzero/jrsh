class Jrsh < Formula
  desc "A Command Line Interface for JasperReports Server"
  homepage "https://github.com/Jaspersoft/jrsh"
  url "https://github.com/Jaspersoft/jrsh/releases/download/v2.0.6/jrsh-2.0.6.zip"
  sha256 "4132106cbaf121d43cd0bc53d0cc72be604500cf8f7829f963211b094f217f56"

  def install
    libexec.install "jrsh.jar"
    bin.write_jar_script libexec/"jrsh.jar", "jrsh"
  end

  test do
    system "#{bin}/jrsh"
  end
end