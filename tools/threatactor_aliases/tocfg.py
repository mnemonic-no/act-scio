import json

data = json.load(open("/tmp/ta.json.tmp"))


output = []

for value in data["values"]:
    output.append(u"{value}: {aliases}".format(
            value=value["value"],
            aliases=",".join(
                value.get("meta", {}).get("synonyms", []))))

res = "\n".join(output)

with open("aliases.cfg", "wb") as f:
   f.write(res.encode("utf8"))
