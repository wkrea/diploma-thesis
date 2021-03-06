export default class GreaterOrEqualTo {
    constructor(left, right) {
        this.left = left
        this.right = right
    }

    interpret(context) {
        return this.left.interpret(context) >= this.right.interpret(context)
    }

    getName() {
        return 'numeric-gte'
    }

    getArguments() {
        return [this.left, this.right]
    }

    getProperties() {
        return {}
    }

    toString() {
        return this.left.toString() + " >= " + this.right.toString()
    }

    clone() {
        return new GreaterOrEqualTo(
            this.left.clone(),
            this.right.clone(),
        )
    }
}
