from flask import Flask, jsonify, request, abort

app = Flask(__name__)

quotes = [
    {"id": 1, "text": "Simplicity is the ultimate sophistication.", "author": "Leonardo da Vinci", "likes": 0},
    {"id": 2, "text": "Talk is cheap. Show me the code.", "author": "Linus Torvalds", "likes": 0},
    {"id": 3, "text": "Premature optimization is the root of all evil.", "author": "Donald Knuth", "likes": 0},
]
_next_id = 4


def find_quote(quote_id):
    return next((q for q in quotes if q["id"] == quote_id), None)


@app.get("/quotes")
def list_quotes():
    author = request.args.get("author")
    q = request.args.get("q")
    result = quotes
    if author is not None:
        result = [quote for quote in result if quote["author"].lower() == author.lower()]
    if q is not None:
        result = [quote for quote in result if q.lower() in quote["text"].lower()]
    return jsonify(result)


@app.get("/quotes/<int:quote_id>")
def get_quote(quote_id):
    quote = find_quote(quote_id)
    if quote is None:
        abort(404)
    return jsonify(quote)


@app.post("/quotes/<int:quote_id>/like")
def like_quote(quote_id):
    quote = find_quote(quote_id)
    if quote is None:
        abort(404)
    quote["likes"] += 1
    return jsonify(quote)


@app.post("/quotes/<int:quote_id>/unlike")
def unlike_quote(quote_id):
    quote = find_quote(quote_id)
    if quote is None:
        abort(404)
    quote["likes"] = max(0, quote["likes"] - 1)
    return jsonify(quote)


@app.post("/quotes")
def create_quote():
    global _next_id
    data = request.get_json(force=True) or {}
    text = data.get("text")
    author = data.get("author")
    if not text or not author:
        abort(400)
    quote = {"id": _next_id, "text": text, "author": author, "likes": 0}
    quotes.append(quote)
    _next_id += 1
    return jsonify(quote), 201


if __name__ == "__main__":
    app.run(debug=True)
