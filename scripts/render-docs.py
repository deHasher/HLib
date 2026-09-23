import argparse
import re
import sys
from pathlib import Path


PROJECT_DIR = Path(__file__).resolve().parents[1]
TOKEN_PATTERN = re.compile(r"\{\{([^{}\r\n]*)\}\}")


def unescape_property(value):
	result = []
	index = 0
	while index < len(value):
		character = value[index]
		if character != "\\":
			result.append(character)
			index += 1
			continue
		index += 1
		if index == len(value):
			break
		character = value[index]
		if character == "u":
			digits = value[index + 1:index + 5]
			if len(digits) != 4 or not re.fullmatch(r"[0-9a-fA-F]{4}", digits):
				raise ValueError("Invalid Unicode escape in gradle.properties")
			result.append(chr(int(digits, 16)))
			index += 5
			continue
		result.append({"t": "\t", "r": "\r", "n": "\n", "f": "\f"}.get(character, character))
		index += 1
	return "".join(result)


def read_properties(path):
	properties = {}
	pending = None
	for line in path.read_text(encoding="utf-8-sig").splitlines():
		line = line.lstrip(" \t\f")
		if pending is None and (not line or line.startswith(("#", "!"))):
			continue
		line = (pending or "") + line
		backslashes = len(line) - len(line.rstrip("\\"))
		if backslashes % 2:
			pending = line[:-1]
			continue
		pending = None
		key_end = len(line)
		escaped = False
		for index, character in enumerate(line):
			if not escaped and character in "=: \t\f":
				key_end = index
				break
			escaped = character == "\\" and not escaped
		value = line[key_end:].lstrip(" \t\f")
		if value.startswith(("=", ":")):
			value = value[1:].lstrip(" \t\f")
		properties[unescape_property(line[:key_end])] = unescape_property(value)
	if pending is not None:
		raise ValueError("Unterminated continuation in gradle.properties")
	return properties


def render_template(path, properties):
	def replace_token(match):
		key = match.group(1).strip()
		if key not in properties:
			raise ValueError(f"Unknown property {{{{{key}}}}} in {path}")
		return properties[key]

	return TOKEN_PATTERN.sub(replace_token, path.read_text(encoding="utf-8-sig")).rstrip("\r\n").encode("utf-8")


def main(argv=None, project_dir=PROJECT_DIR):
	parser = argparse.ArgumentParser(description="Render README and GitHub Wiki using gradle.properties.")
	parser.add_argument("--check", action="store_true", help="Validate templates and check README without writing files.")
	parser.add_argument("--wiki-dir", type=Path, help="Wiki destination; defaults to build/wiki in the project.")
	parser.add_argument("--property", dest="property_name", help="Print one Gradle property without rendering documentation.")
	args = parser.parse_args(argv)
	if args.property_name is not None and (args.check or args.wiki_dir is not None):
		parser.error("--property cannot be combined with --check or --wiki-dir")
	try:
		properties = read_properties(project_dir / "gradle.properties")
		if args.property_name is not None:
			if args.property_name not in properties:
				raise ValueError(f"Unknown Gradle property: {args.property_name}")
			print(properties[args.property_name])
			return 0
		wiki_source = project_dir / "docs" / "wiki"
		wiki_templates = sorted(wiki_source.rglob("*.md"))
		if not wiki_templates:
			raise ValueError(f"No Wiki templates found in {wiki_source}")
		wiki_destination = args.wiki_dir if args.wiki_dir is not None else project_dir / "build" / "wiki"
		readme_path = project_dir / "README.md"
		rendered = {readme_path: render_template(project_dir / "docs" / "README.md", properties)}
		for template in wiki_templates:
			rendered[wiki_destination / template.relative_to(wiki_source)] = render_template(template, properties)
		if args.check:
			if not readme_path.exists() or readme_path.read_text(encoding="utf-8").encode("utf-8") != rendered[readme_path]:
				raise ValueError("README.md is out of date. Run: python scripts/render-docs.py")
			print("Documentation templates are valid and README.md is up to date.")
			return 0
		for destination, content in rendered.items():
			if not destination.exists() or destination.read_bytes() != content:
				destination.parent.mkdir(parents=True, exist_ok=True)
				destination.write_bytes(content)
		print(f"Rendered README.md and {len(wiki_templates)} Wiki pages.")
		return 0
	except (OSError, UnicodeError, ValueError) as error:
		print(f"Documentation error: {error}", file=sys.stderr)
		return 1


if __name__ == "__main__":
	raise SystemExit(main())